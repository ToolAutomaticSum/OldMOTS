
from keras import backend as K


class AttentionDecoderCell(Layer):
    def __init__(self, units,
                 activation='tanh',
                 recurrent_activation='hard_sigmoid',
                 use_bias=True,
                 kernel_initializer='glorot_uniform',
                 recurrent_initializer='orthogonal',
                 bias_initializer='zeros',
                 unit_forget_bias=True,
                 kernel_regularizer=None,
                 bias_regularizer=None,
                 activity_regularizer=None,
                 kernel_constraint=None,
                 bias_constraint=None,
                 dropout=0,
                 recurrent_dropout=0,
                 **kwargs):
        """
        Implements an AttentionDecoder that takes in a sequence encoded
        by an encoder and outputs the decoded states
        :param units: dimension of the hidden state and the attention matrices
        :param output_dim: the number of labels in the output space

        References:
            Bahdanau, Dzmitry
        """
        super(AttentionDecoderCell, self).__init__(**kwargs)

        self.units = units
        self.activation = activations.get(activation)
        self.recurrent_activation = activations.get(recurrent_activation)
        self.use_bias = use_bias

        self.kernel_initializer = initializers.get(kernel_initializer)
        self.recurrent_initializer = initializers.get(recurrent_initializer)
        self.bias_initializer = initializers.get(bias_initializer)
        self.unit_forget_bias = unit_forget_bias

        self.kernel_regularizer = regularizers.get(kernel_regularizer)
        self.recurrent_regularizer = regularizers.get(kernel_regularizer)
        self.bias_regularizer = regularizers.get(bias_regularizer)
        self.activity_regularizer = regularizers.get(activity_regularizer)

        self.kernel_constraint = constraints.get(kernel_constraint)
        self.recurrent_constraint = contraints.get(kernel_constraint)
        self.bias_constraint = constraints.get(bias_constraint)

        self.dropout = min(1., max(0., dropout))
        self.recurrent_dropout = min(1., max(0., recurrent_dropout))
        self.state_size = (self.units, self.units)
        self.output_size = self.units
        self._dropout_mask = None
        self._recurrent_dropout_mask = None

    def build(self, input_shape):
        """
            See Appendix 2 of Bahdanau 2014
            for model details that correspond to the matrices here.
        """
        input_dim = input_shape[-1]

        if type(self.recurrent_initializer).__name__ == 'Identity':
            def recurrent_identity(shape, gain=1., dtype=None):
                del dtype
                return gain * np.concatenate([np.identity(shape[0])] *
                                             (shape[1] // shape[0]), axis=1)
            self.recurrent_initializer = recurrent_identity

        self.states = [None, None] # y, s


        self.kernel = self.add_weight(shape=(input_dim, self.units * 4),
                                  name='kernel',
                                  initializer=self.kernel_initializer,
                                  regularizer=self.kernel_regularizer,
                                  contraint=self.kernel_constraint)
        self.recurrent_kernel = self.add_weight(shape=(input_dim, self.units *
                                                      4),
                                  name='recurrent_kernel',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)

        if self.use_bias:
            if self.unit_forget_bias:
                def bias_initializer(_, *args, **kwargs):
                    return K.concatenate([
                        self.bias_initializer((self.units,), *args, **kwargs),
                        initializers.Ones()((self.units,), *args, **kwargs),
                        self.bias_initializer((self.units * 2,), *args,
                                              **kwargs),
                        ])
            else:
                self.bias_initializer = bias_initializer
            self.bias = self.add_weight(shape=(self.units * 4,),
                                      name='bias',
                                      initializer=self.bias_initializer,
                                      regularizer=self.bias_regularizer,
                                      contraint=self.bias_constraint)
        else:
            self.bias = None

        self.kernel_i = self.kernel[:, :self.units]
        self.kernel_f = self.kernel[:, self.units: self.units * 2]
        self.kernel_c = self.kernel[:, self.units * 2: self.units * 3]
        self.kernel_o = self.kernel[:, self.units * 3:]

        self.recurrent_kernel_i = self.recurrent_kernel[:, :self.units]
        self.recurrent_kernel_f = self.recurrent_kernel[:, self.units: self.units * 2]
        self.recurrent_kernel_c = self.recurrent_kernel[:, self.units * 2: self.units * 3]
        self.recurrent_kernel_o = self.recurrent_kernel[:, self.units * 3:]

        if self.use_bias:
            self.bias_i = self.bias[:self.units]
            self.bias_f = self.bias[self.units: self.units * 2]
            self.bias_c = self.bias[self.units * 2: self.units * 3]
            self.bias_o = self.bias[self.units * 3:]
        else:
            self.bias_i = None
            self.bias_f = None
            self.bias_c = None
            self.bias_o = None
        self.built = True

    def call(self, x):
        if 0 < self.dropout < 1 and self._dropout_mask is None:
            self._dropout_mask = _generate_dropout_mask(K.ones_like(inputs),
                                                       self.dropout,
                                                       training=training,
                                                       count=4)
        if (0 < self.recurrent_dropout < 1 and self._recurrent_dropout_mask is
            None):
            self._recurrent_dropout_mask = _generate_dropout_mask(
                                                K.ones_like(states[0]),
                                                self.recurrent_dropout,
                                                training=training,
                                                count=4)


        # store the whole sequence so we can "attend" to it at each timestep
        self.x_seq = x

        self._uxpb = _time_distributed_dense(self.x_seq, self.U_a,
                                             bias=self.b_a,
                                            input_dim=self.input_dim,
                                            timesteps=self.timesteps,
                                            output_dim=self.units)
        return super(AttentionDecoder, self).call(x)

    def get_initial_state(self, inputs):
        print('inputs shape:', inputs.get_shape())

        s0 = activations.tanh(K.dot(inputs[:, 0], self.W_s))

        y0 = K.zeros_like(inputs)
        y0 = K.sum(y0, axis=(1, 2))
        y0 = K.expand_dims(y0)
        y0 = K.tile(y0, [1, self.output_dim])

        return [y0, s0]

    def step(self, x, states):
        ytm, stm = states

        _stm = K.repeat(stm, self.timesteps)

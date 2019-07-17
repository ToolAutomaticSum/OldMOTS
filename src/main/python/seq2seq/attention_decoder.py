
from keras import backend as K


def _time_distributed_dense(x, w, b=None, dropout=None,
                           input_dim=None, output_dim=None,
                           timesteps=None, training=None):
    """
    Apply `y . w + b` for every temporal slice y of x.

    # Arguments
        x: input tensor
        w: weight matrix
        b: optional bias vector
        dropout: wehter to apply dropout (same dropout mask
            for every temporal slice of the input).
        input_dim: integer; optional dimensionality of the input
        output_dim: integer; optional dimentionality of the output
        timesteps: integer; optional number of timesteps
        training: training phase tensor or boolean

    # Returns
        Output tensor
    """

    if not input_dim:
        input_dim = K.shape(x)[2]
    if not timesteps:
        timesteps = K.shape(x)[1]
    if not output_dim:
        output_dim = K.shape(w)[1]

    if dropout is not None and 0. < dropout < 1.:
        # apply the same dropout pattern at every timestep
        ones = K.ones_like(K.reshape(x[:,0, :], (-1, input_dim)))
        dropout_matrix = K.dropout(ones, dropout)
        expanded_dropout_matrix = K.repeat(dropout_matrix, timesteps)
        x = K.in_train_phase(x * expanded_dropout_matrix, x, training=training)

    # maybe below is more clear implementation compared to older keras
    # at least it works the same for tensorflow, but not tested on other
    # backends

    x = K.dot(x, w)
    if b is not None:
        x = K.bias_add(x, b)
    return x


class AttentionDecoder(Recurrent):
    def __init__(self, units, output_dim,
                 activation='tanh',
                 return_probabilities=False,
                 name='AttentionDecoder',
                 kernel_initializer='glorot_uniform',
                 recurrent_initializer='orthogonal',
                 bias_initializer='zeros',
                 kernel_regularizer=None,
                 bias_regularizer=None,
                 activity_regularizer=None,
                 kernel_constraint=None,
                 bias_constraint=None,
                 **kwargs):
        """
        Implements an AttentionDecoder that takes in a sequence encoded
        by an encoder and outputs the decoded states
        :param units: dimension of the hidden state and the attention matrices
        :param output_dim: the number of labels in the output space

        References:
            Bahdanau, Dzmitry
        """

        self.units = units
        self.output_dim = output_dim
        self.return_probabilities = return_probabilities
        self.activation = activations.get(activation)
        self.kernel_initializer = initializers.get(kernel_initializer)
        self.recurrent_initializer = initializers.get(recurrent_initializer)
        self.bias_initializer = initializers.get(bias_initializer)

        self.kernel_regularizer = regularizers.get(kernel_regularizer)
        self.recurrent_regularizer = regularizers.get(kernel_regularizer)
        self.bias_regularizer = regularizers.get(bias_regularizer)
        self.activity_regularizer = regularizers.get(activity_regularizer)

        self.kernel_constraint = constraints.get(kernel_constraint)
        self.recurrent_constraint = contraints.get(kernel_constraint)
        self.bias_constraint = constraints.get(bias_constraint)

        super(AttentionDecoder, self).__init__(**kwargs)
        self.name = name
        # self.return_sequences = True # must return sequences

    def build(self, input_shape):
        """
            See Appendix 2 of Bahdanau 2014
            for model details that correspond to the matrices here.
        """

        self.batch_size, self.timesteps, self.input_dim, = input_shape

        if self.stateful:
            super(AttentionDecoder, self).reset_states()

        self.states = [None, None] # y, s

        """
            Matrices for creating the context vector
        """

        self.V_a = self.add_weight(shape=(self.units,),
                                  name='V_a',
                                  initializer=self.kernel_initializer,
                                  regularizer=self.kernel_regularizer,
                                  contraint=self.kernel_constraint)
        self.W_a = self.add_weight(shape=(self.units,),
                                  name='W_a',
                                  initializer=self.kernel_initializer,
                                  regularizer=self.kernel_regularizer,
                                  contraint=self.kernel_constraint)
        self.U_a = self.add_weight(shape=(self.units,),
                                  name='U_a',
                                  initializer=self.kernel_initializer,
                                  regularizer=self.kernel_regularizer,
                                  contraint=self.kernel_constraint)
        self.b_a = self.add_weight(shape=(self.units,),
                                  name='b_a',
                                  initializer=self.bias_initializer,
                                  regularizer=self.bias_regularizer,
                                  contraint=self.bias_constraint)

        """
            Matrices for the r (reset) gate
        """
        self.C_r = self.add_weight(shape=(self.units,),
                                  name='C_r',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.U_r = self.add_weight(shape=(self.units,),
                                  name='U_r',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.W_r = self.add_weight(shape=(self.units,),
                                  name='W_r',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.b_r = self.add_weight(shape=(self.units,),
                                  name='b_r',
                                  initializer=self.bias_initializer,
                                  regularizer=self.bias_regularizer,
                                  contraint=self.bias_constraint)


        """
            Matrices for the z (update) gate
        """
        self.C_z = self.add_weight(shape=(self.units,),
                                  name='C_z',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.U_z = self.add_weight(shape=(self.units,),
                                  name='U_z',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.W_z = self.add_weight(shape=(self.units,),
                                  name='W_z',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.b_z = self.add_weight(shape=(self.units,),
                                  name='b_z',
                                  initializer=self.bias_initializer,
                                  regularizer=self.bias_regularizer,
                                  contraint=self.bias_constraint)


        """
            Matrices for the proposal
        """
        self.C_p = self.add_weight(shape=(self.units,),
                                  name='C_p',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.U_p = self.add_weight(shape=(self.units,),
                                  name='U_p',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.W_p = self.add_weight(shape=(self.units,),
                                  name='W_p',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.b_p = self.add_weight(shape=(self.units,),
                                  name='b_p',
                                  initializer=self.bias_initializer,
                                  regularizer=self.bias_regularizer,
                                  contraint=self.bias_constraint)

        """
            Matrices for the final prediction vector
        """
        self.C_o = self.add_weight(shape=(self.units,),
                                  name='C_o',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.U_o = self.add_weight(shape=(self.units,),
                                  name='U_o',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.W_o = self.add_weight(shape=(self.units,),
                                  name='W_o',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)
        self.b_o = self.add_weight(shape=(self.units,),
                                  name='b_o',
                                  initializer=self.bias_initializer,
                                  regularizer=self.bias_regularizer,
                                  contraint=self.bias_constraint)

        # for creating the initial state
        self.W_s = self.add_weight(shape=(self.units,),
                                  name='W_s',
                                  initializer=self.recurrent_initializer,
                                  regularizer=self.recurrent_regularizer,
                                  contraint=self.recurrent_constraint)

        self.input_spec = [InputSpec(shape=(self.batch_size, self.timesteps,
                                            self.input_dim))]
        self.built = True

    def call(self, x):
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

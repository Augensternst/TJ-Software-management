import numpy as np
import matplotlib.pyplot as plt
import numpy.linalg as lg

t=np.arange(1,17,1)
y=np.array([4,6.4,8,8.8,9.22,9.5,9.7,9.86,10,10.20,10.32,10.42,10.5,10.55,10.58,10.6
])
plt.figure()
plt.plot(t,y,'k*')
# y=at^2+bt+c

A=np.c_[t**2,t,np.ones(t.shape)]

w=lg.inv(A.T.dot(A)).dot(A.T).dot(y)

plt.plot(t,w[0]*t**2+w[1]*t+w[2])
plt.show()
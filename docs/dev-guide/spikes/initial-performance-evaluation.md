## Initial Performance Evaluation
Measure engine runtime <br/> _leveraging the `Taurus` framework_

### `localhost` <-> `IBM Cloud`
using `k8s` _port forwarding_

| # of Measures | # of Patients | Total Time (sec) | Seconds/Execution |
| ------------: | ------------: | ---------------: | ----------------: |
|             1 |             1 |            11.96 |             11.96 |
|             1 |           100 |           178.94 |              1.79 |
|             1 |         1,000 |            1,680 |              1.68 |
|            10 |             1 |            12.99 |              1.30 |

### intra-`IBM Cloud`

| # of Measures | # of Patients | Total Time (sec) | Seconds/Execution |
| ------------: | ------------: | ---------------: | ----------------: |
|             1 |             1 |            10.62 |             10.62 |
|             1 |           100 |           113.92 |              1.14 |
|             1 |         1,000 |           975.36 |           0.97536 |
|            10 |             1 |            10.82 |              1.08 |
|            10 |           100 |           914.08 |              0.91 |
|            10 |         1,000 |             7574 |              0.76 |

# qbe-baselines



### S4 (2015)

- Paper: [https://dl.acm.org/doi/10.1145/2723372.2749452](https://dl.acm.org/doi/10.1145/2723372.2749452)
- Slides: [http://www.cs.columbia.edu/~fotis/pubs/posters/s4.pdf](http://www.cs.columbia.edu/~fotis/pubs/posters/s4.pdf)
- Source code: *Not Available*





### REGAL (2017)

- Paper
  - REGAL (2017): [http://www.vldb.org/pvldb/vol10/p1394-tan.pdf](http://www.vldb.org/pvldb/vol10/p1394-tan.pdf)
  - REGAL+ (2018): [http://www.vldb.org/pvldb/vol11/p1982-tan.pdf](http://www.vldb.org/pvldb/vol11/p1982-tan.pdf)
- Source code: [https://github.com/weichit/Regal](https://github.com/weichit/Regal)





### FastQRE (2018)

- Paper: [https://www.ics.uci.edu/~dvk/pub/C25_SIGMOD18_dvk.pdf](https://www.ics.uci.edu/~dvk/pub/C25_SIGMOD18_dvk.pdf)
- Source code: *Not Available*





### SQuID (2019)

- Paper: [https://arxiv.org/pdf/1906.10322](https://arxiv.org/pdf/1906.10322)
- Source code: [https://bitbucket.org/afariha/squid/src/master/](https://bitbucket.org/afariha/squid/src/master/)
- Project Page: [http://squid.cs.umass.edu](http://squid.cs.umass.edu)





### DuoQuest (2020)

- Paper: [https://arxiv.org/pdf/2003.07438](https://arxiv.org/pdf/2003.07438)
- Source code: [https://github.com/umich-dbgroup/duoquest](https://github.com/umich-dbgroup/duoquest)





### Comparisons

##### Query class

|          | Join | Projection | Selection | Aggregation |
| -------- | :--: | :--------: | :-------: | :---------: |
| S4       |  ⭕️   |            |           |             |
| REGAL+   |  ⭕️*  |     ⭕️      |     ⭕️     |      ⭕️      |
| FastQRE  |  ⭕️   |     ⭕️      |           |             |
| SQuID    |  ⭕️   |     ⭕️      |     ⭕️     |      ⭕️      |
| DuoQuest |  ⭕️   |     ⭕️      |     ⭕️     |      ⭕️      |

*REGAL does not support join queries, but REGAL+ does.
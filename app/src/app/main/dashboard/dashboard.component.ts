import { Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Apollo, gql } from 'apollo-angular';
import { EChartsOption } from 'echarts';
import { Observable, Subscription } from 'rxjs';

const gqlMap: any = {
  "accountArea15days": [gql`
    query accountArea15days {
      getAccountArea15days {
        _id
        type
        bucketSize
        acct
        areaCode
        sumBet
        sumCasinoWin
        sumTheorWin
        noOfTxn
        avgBet
        avgCasinoWin
        avgTheorWin
      }
    }`, gql`
    subscription accountArea15days {
      watchAccountArea15days {
        _id
        type
        bucketSize
        acct
        areaCode
        sumBet
        sumCasinoWin
        sumTheorWin
        noOfTxn
        avgBet
        avgCasinoWin
        avgTheorWin
      }
    }`],


  "accountArea3mins": [gql`
      query accountArea3mins {
        getAccountArea3mins {
          _id
          type
          bucketSize
          acct
          areaCode
          sumBet
          sumCasinoWin
          sumTheorWin
          noOfTxn
          avgBet
          avgCasinoWin
          avgTheorWin
        }
      }`, gql`
      subscription accountArea3mins {
        watchAccountArea3mins {
          _id
          type
          bucketSize
          acct
          areaCode
          sumBet
          sumCasinoWin
          sumTheorWin
          noOfTxn
          avgBet
          avgCasinoWin
          avgTheorWin
        }
      }`],


  "accountCasinoArea1day": [gql`
          query accountCasinoArea1day {
            getAccountCasinoArea1day {
              _id
              type
              bucketSize
              acct
              casinoCode
              areaCode
              sumBet
              sumCasinoWin
              sumTheorWin
              noOfTxn
              avgBet
              avgCasinoWin
              avgTheorWin
            }
          }`, gql`
          subscription accountCasinoArea1day {
            watchAccountCasinoArea1day {
              _id
              type
              bucketSize
              acct
              casinoCode
              areaCode
              sumBet
              sumCasinoWin
              sumTheorWin
              noOfTxn
              avgBet
              avgCasinoWin
              avgTheorWin
            }
          }`],




  "accountCasinoArea3mins": [gql`
        query accountCasinoArea3mins {
          getAccountCasinoArea3mins {
            _id
            type
            bucketSize
            acct
            casinoCode
            areaCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`, gql`
        subscription accountCasinoArea3mins {
          watchAccountCasinoArea3mins {
            _id
            type
            bucketSize
            acct
            casinoCode
            areaCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`],




  "accountCasino1day": [gql`
        query accountCasino1day {
          getAccountCasino1day {
            _id
            type
            bucketSize
            acct
            casinoCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`, gql`
        subscription accountCasino1day {
          watchAccountCasino1day {
            _id
            type
            bucketSize
            acct
            casinoCode
            sumBet
            sumCasinoWin
            sumTheorWin
            noOfTxn
            avgBet
            avgCasinoWin
            avgTheorWin
          }
        }`]
}

const tables = [
  [1025, 1000],
  [1055, 960],
  [1085, 920],
  [1115, 960],
  [1145, 1000],
  [1115, 1040],
  [1085, 1080],
  [1055, 1040],

  [1255, 1000],
  [1285, 960],
  [1315, 920],
  [1345, 960],
  [1375, 1000],
  [1345, 1040],
  [1315, 1080],
  [1285, 1040],


  [1025, 785],
  [1055, 745],
  [1085, 705],
  [1115, 745],
  [1145, 785],
  [1115, 825],
  [1085, 865],
  [1055, 825],


  [1255, 785],
  [1285, 745],
  [1315, 705],
  [1345, 745],
  [1375, 785],
  [1345, 825],
  [1315, 865],
  [1285, 825],



  [525, 570],
  [555, 530],
  [585, 490],
  [615, 530],
  [645, 570],
  [615, 610],
  [585, 650],
  [555, 610],


  [755, 570],
  [785, 530],
  [815, 490],
  [845, 510],
  [875, 570],
  [845, 610],
  [815, 650],
  [785, 610],


  [775, 355],
  [805, 315],
  [835, 275],
  [865, 315],
  [895, 355],
  [865, 395],
  [835, 435],
  [805, 395],


  [1005, 355],
  [1035, 315],
  [1065, 275],
  [1095, 315],
  [1125, 355],
  [1095, 395],
  [1065, 435],
  [1035, 395],





  [1025, 570],
  [1055, 530],
  [1085, 490],
  [1115, 530],
  [1145, 570],
  [1115, 610],
  [1085, 650],
  [1055, 610],


  [1255, 570],
  [1285, 530],
  [1315, 490],
  [1345, 510],
  [1375, 570],
  [1345, 610],
  [1315, 650],
  [1285, 610],


  [1025, 355],
  [1055, 315],
  [1085, 275],
  [1115, 315],
  [1145, 355],
  [1115, 395],
  [1085, 435],
  [1055, 395],


  [1255, 355],
  [1285, 315],
  [1315, 275],
  [1345, 315],
  [1375, 355],
  [1345, 395],
  [1315, 435],
  [1285, 395],


  /*[1300, 1000],
  [1080, 790],
  [1300, 790],
  [640, 580],
  [860, 580],
  [1080, 580],
  [1300, 580],
  [640, 370],
  [860, 370],
  [1080, 370],
  [1300, 370]*/
];

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  public chartQuery!: any;
  public chartResult!: Observable<any>;
  chartSubscription!: Subscription;

  data: any[] = [];
  heatMap!: EChartsOption;
  heatMapUpdate!: EChartsOption;

  @ViewChild("chart")
  chart!: ElementRef;


  @ViewChild(MatSort) sort!: MatSort;

  type = 'accountArea';
  duration = '15days';

  public query!: any;
  public queryResult!: Observable<any>;
  subscription!: Subscription;
  dataSource: MatTableDataSource<any> = new MatTableDataSource();
  displayedColumns: string[] = ['acct', 'casinoCode', 'areaCode', 'sumBet', 'avgBet'];


  constructor(private readonly apollo: Apollo) { }
  ngOnInit(): void {
    let xData: number[] = [];
    let yData: number[] = [];

    for (let i = 0; i <= 1600; i++) {
      xData.push(i);
    }
    for (let j = 0; j < 1200; j++) {
      yData.push(j);
    }

    this.heatMap = {
      grid: {
        left: 0,
        right: 0,
        top: 0,
        bottom: 0
      },
      tooltip: {},
      xAxis: {
        type: 'category',
        show: false,
        data: xData,
      },
      yAxis: {
        type: 'category',
        show: false,
        data: yData,
      },
      /*visualMap: {
        top: 'top',
        calculable: false,
        inRange: {
          color: ['white', 'red']
        },
      },*/
      graphic: {
        elements: [
          {
            type: 'image',
            style: {
              image: 'assets/floorplan/casino-layout.jpg'
            },
            left: 0,
            right: 0,
            top: 0,
            bottom: 0
          }
        ]
      },
      series: [
        {
          type: 'effectScatter',
          data: this.data,
          symbolSize: function (val) {            
            return val[2]*0.2;
          },
        },
      ],
    };
    /*this.timer = setInterval(() => {
      this.heatMapUpdate = {
        series: [
          {
            data: this.generateData(),
          },
        ],
      };
    }, 1500);*/


    if (this.chartSubscription) {
      this.chartSubscription.unsubscribe();
    }
    this.chartQuery = this.apollo
      .watchQuery({
        query: gql`
          query casinoAreaLocation1day {
            getCasinoAreaLocation1day {
              locnIndex
              locnCode
              areaCode
              casinoCode
              headCount
            }
          }`,
      });
    this.chartResult = this.chartQuery.valueChanges;
    this.chartSubscription = this.chartResult.subscribe((result) => {
      let data: number[][] = [];
      tables.forEach((table, index) => {
        const counts = result.data[Object.keys(result.data)[0]].filter((i: any) => i.locnIndex == index).map((i: any) => i.headCount);
        if(counts && counts.length)
          data.push([...table, counts.reduce((sum: any, num: any) => sum + num)]);
      });
      this.heatMapUpdate = {
        series: [
          {
            data: data,
          },
        ],
      };
    });
    this.chartQuery.subscribeToMore({
      document: gql`
        subscription casinoAreaLocation1day {
          watchCasinoAreaLocation1day {
              locnIndex
              locnCode
              areaCode
              casinoCode
              headCount
          }
        }`,
      updateQuery: (prev: any, result: any) => {
        if (!result.subscriptionData.data) return prev;
        const gKey = "getCasinoAreaLocation1day";
        const wKey = Object.keys(result.subscriptionData.data)[0];
        const newItem = result.subscriptionData.data[`${wKey}`];
        let res: any[] = [];
        if (prev[gKey]) {
          res = [...prev[gKey]];
        }
        const i = res.findIndex((v: any) => v._id == newItem._id);
        if (i === -1) res.push(newItem);
        else res.splice(i, 1, newItem);

        return { [`${gKey}`]: res };

        //console.log(result.subscriptionData.data[Object.keys(result.subscriptionData.data)[0]]);
      },
      onError: (err: any) => console.error(err)
    });

    this.change();
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
    setTimeout(() => {
      this.heatMapUpdate = {
        graphic: {
          elements: [
            {
              style: {
                width: this.chart.nativeElement.clientWidth, height: this.chart.nativeElement.clientHeight
              },
            }
          ]
        }
      }
    }, 100);
  }
  @HostListener('window:resize', ['$event'])
  onResize() {
    this.heatMapUpdate = {
      graphic: {
        elements: [
          {
            style: {
              width: this.chart.nativeElement.clientWidth, height: this.chart.nativeElement.clientHeight
            },
          }
        ]
      }
    }
  }
  /*generateData() {
    let data = [];
    for (let i = 0; i < 12; i++) {
      data.push([...tables[i], (Math.random() * 10000)])
    }
    return data;
  }*/

  change() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    const gql = gqlMap[this.type + this.duration];
    if (gql) {
      this.query = this.apollo
        .watchQuery({
          query: gql[0],
        });
      this.queryResult = this.query.valueChanges;
      this.subscription = this.queryResult.subscribe((result) => {
        this.dataSource.data = result.data[Object.keys(result.data)[0]];
      });
      this.query.subscribeToMore({
        document: gql[1],
        updateQuery: (prev: any, result: any) => {
          if (!result.subscriptionData.data) return prev;
          const gKey = "get" + String(this.type).charAt(0).toUpperCase() + String(this.type).slice(1) + this.duration;
          const wKey = Object.keys(result.subscriptionData.data)[0];
          const newItem = result.subscriptionData.data[`${wKey}`];

          let res: any[] = [];
          if (prev[gKey]) {
            res = [...prev[gKey]];
          }
          const i = res.findIndex((v: any) => v._id == newItem._id);
          if (i === -1) res.push(newItem);
          else res.splice(i, 1, newItem);

          return { [`${gKey}`]: res };

          //console.log(result.subscriptionData.data[Object.keys(result.subscriptionData.data)[0]]);
        },
        onError: (err: any) => console.error(err)
      });
    }
  }
}

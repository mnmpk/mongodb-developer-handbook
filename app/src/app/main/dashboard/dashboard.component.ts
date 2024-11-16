import { Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { EChartsOption } from 'echarts';
import { Observable, Subscription } from 'rxjs';


const tables = [
  [1080, 1000],
  [1300, 1000],
  [1080, 790],
  [1300, 790],
  [640, 580],
  [860, 580],
  [1080, 580],
  [1300, 580],
  [640, 370],
  [860, 370],
  [1080, 370],
  [1300, 370]
];

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  public query!: any;
  public queryResult!: Observable<any>;
  subscription!: Subscription;

  data: any[] = [];
  heatMap!: EChartsOption;
  heatMapUpdate!: EChartsOption;

  @ViewChild("chart")
  chart!: ElementRef;



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
            return val[2] / 100;
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


    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    this.query = this.apollo
      .watchQuery({
        query: gql`
          query casinoAreaLocation1day {
            getCasinoAreaLocation1day {
              locnCode
              areaCode
              casinoCode
              headCount
            }
          }`,
      });
    this.queryResult = this.query.valueChanges;
    this.subscription = this.queryResult.subscribe((result) => {
      let data: number[][] = [];
      result.data[Object.keys(result.data)[0]].forEach((item: any) => {
        const charCode = item.locnCode.charCodeAt(0);
        data.push([...tables[charCode % 12], (item.headCount) * 500])
      });
      this.heatMapUpdate = {
        series: [
          {
            data: data,
          },
        ],
      };
    });
    this.query.subscribeToMore({
      document: gql`
        subscription casinoAreaLocation1day {
          watchCasinoAreaLocation1day {
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
  }

  ngAfterViewInit() {
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
}

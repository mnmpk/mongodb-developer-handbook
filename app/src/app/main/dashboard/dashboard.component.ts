import { Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { EChartsOption } from 'echarts';


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
  data:any[]=[];
  heatMap!: EChartsOption;
  heatMapUpdate!: EChartsOption;

  @ViewChild("chart")
  chart!: ElementRef;
  private timer: any;

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
          data: this.generateData(),
          symbolSize: function (val) {
            return val[2] / 100;
          },
        },
      ],
    };
    this.timer = setInterval(() => {
      this.heatMapUpdate = {
        series: [
          {
            data: this.generateData(),
          },
        ],
      };
    }, 1500);
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
  generateData() {
    let data = [];
    for (let i = 0; i < 12; i++) {
      data.push([...tables[i], (Math.random() * 10000)])
    }
    return data;
  }
}

import { Component, ElementRef, ViewChild } from '@angular/core';
import { EChartsOption } from 'echarts';

@Component({
  selector: 'app-change-stream',
  templateUrl: './change-stream.component.html',
  styleUrl: './change-stream.component.scss'
})
export class ChangeStreamComponent {
  heatMap!: EChartsOption;
  heatMapUpdate!: EChartsOption;

  @ViewChild("chart")
  chart!: ElementRef;

  ngOnInit(): void {
    let { data, xData, yData } = this.generateData();
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
          data: data,
          symbolSize: function (val) {
            return val[2] / 100;
          },
        },
      ],
    };

  }

  ngAfterViewInit() {
    setTimeout(()=>{

      console.log(this.chart);
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
    },1000);
  }
  generateData() {
    let xData: number[] = [];
    let yData: number[] = [];

    let data = [];
    for (let i = 0; i <= 200; i++) {
      for (let j = 0; j <= 100; j++) {
        if (i % 3 == 0 && j % 7 == 1)
          data.push([i, j, i + j * 10]);
      }
      xData.push(i);
    }
    for (let j = 0; j < 100; j++) {
      yData.push(j);
    }
    return { data, xData, yData };
  }
}

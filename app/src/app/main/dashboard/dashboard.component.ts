import { Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { EChartsOption } from 'echarts';
import { Observable, Subscription } from 'rxjs';
import { RxStompService } from '../../shared/rx-stomp.service';

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
  styleUrl: './dashboard.component.scss',
  standalone: false
})
export class DashboardComponent {
  public chartQuery!: any;
  public chartResult!: Observable<any>;
  chartSubscription!: Subscription;

  data: number[][] = [];
  heatMap!: EChartsOption;
  heatMapUpdate!: EChartsOption;

  @ViewChild("chart")
  chart!: ElementRef;


  @ViewChild(MatSort) sort!: MatSort;

  type = 'acct-areaCode';
  duration = '15days';

  public query!: any;
  public queryResult!: Observable<any>;
  subscription!: Subscription;
  dataSource: MatTableDataSource<any> = new MatTableDataSource();
  displayedColumns: string[] = ['acct', 'casinoCode', 'areaCode', 'sumBet', 'avgBet'];


  constructor(private stompService: RxStompService) { }
  ngOnInit(): void {
    this.stompService.watch('/sync').subscribe(msg => {
      var res = JSON.parse(msg.body);
      if (res.content.type == "REFRESH") {
        var obj = res.content.data;
        if (this.type == obj.type && this.duration == obj.bucketSize) {
          const currentData = this.dataSource.data;
          const foundItem = currentData.find(item => item._id === obj._id);
          if (!foundItem) {
            currentData.push(obj);
          } else {
            const updatedItem = { ...foundItem, ...obj };
            const index = currentData.indexOf(foundItem);
            currentData[index] = updatedItem;
          }

          this.dataSource.data = currentData;
        }
      } else {
        var obj = res.content;
        tables.forEach((table, index) => {
          if(index==obj.locnIndex){
            this.data.push([...table, obj.headCount]);
          }
        });
        this.heatMapUpdate = {
          series: [
            {
              data: this.data,
            },
          ],
        };
      }
    });
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
            return val[2] * 0.2;
          },
        },
      ],
    };
    
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

  change() {
    //TODO: query initial data based on type and duration
    this.dataSource.data = [];
  }
}

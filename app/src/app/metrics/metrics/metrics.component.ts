import { Component } from '@angular/core';
import { EChartsOption } from 'echarts';
import { MetricsService } from '../metrics.service';
import { Stat } from '../../shared/models/stats';

@Component({
  selector: 'app-metrics',
  templateUrl: './metrics.component.html',
  styleUrl: './metrics.component.scss'
})
export class MetricsComponent {
  throughput!: EChartsOption;
  throughputUpdate!: EChartsOption;
  latency!: EChartsOption;
  latencyUpdate!: EChartsOption;
  constructor(private metricsService: MetricsService) { }
  ngOnInit(): void {
    this.throughput = {
      title: {
        text: 'Throughput (TPS)',
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          params = params[0];

          return (
            params.value.join("<br/>")
          );
        },
        axisPointer: {
          animation: false,
        },
      },
      xAxis: {
        type: 'category'
      },
      yAxis: {
        type: 'value',
        boundaryGap: [0, '100%'],
      },
      series: [
        {
          type: 'line',
          showSymbol: false,
          data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.ops, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
        },
      ],
    };
    this.latency = {
      title: {
        text: 'Latency (ms)',
      },
      legend: {
        data: ['min', 'avg', 'max'],
        align: 'left',
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          params = params[0];

          return (
            params.value.join("<br/>")
          );
        },
        axisPointer: {
          animation: false,
        },
      },
      xAxis: {
        type: 'category'
      },
      yAxis: {
        type: 'value',
        boundaryGap: [0, '100%'],
      },
      series: [
        {
          name: 'min',
          type: 'line',
          showSymbol: false,
          data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.min, s.min, s.avg, s.max, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
        },
        {
          name: 'avg',
          type: 'line',
          showSymbol: false,
          data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.avg, s.min, s.avg, s.max, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
        },
        {
          name: 'max',
          type: 'line',
          showSymbol: false,
          data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.max, s.min, s.avg, s.max, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
        },
      ],
    };
    MetricsService.update$.subscribe((stat: Stat<any>) => {
      this.throughputUpdate = {
        series: [
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.ops, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
          },
        ],
      };
      this.latencyUpdate = {
        series: [
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.min, s.min, s.avg, s.max, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
          },
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.avg, s.min, s.avg, s.max, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
          },
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i + 1, s.max, s.min, s.avg, s.max, s.startAt, s.endAt, s.duration, s.workload.qty, s.workload.numWorkers] } })
          },
        ],
      };
    });
  }
}

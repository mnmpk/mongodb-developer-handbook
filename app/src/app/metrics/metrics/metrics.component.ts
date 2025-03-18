import { Component } from '@angular/core';
import { EChartsOption } from 'echarts';
import { MetricsService } from '../metrics.service';
import { Stat } from '../../shared/models/stats';
import { WorkloadType } from '../../shared/models/workload';
import { UtilityService } from '../../shared/utility.service';

@Component({
    selector: 'app-metrics',
    templateUrl: './metrics.component.html',
    styleUrl: './metrics.component.scss',
    standalone: false
})
export class MetricsComponent {
  throughput!: EChartsOption;
  throughputUpdate!: EChartsOption;
  latency!: EChartsOption;
  latencyUpdate!: EChartsOption;
  constructor(private utilityService: UtilityService, public metricsService: MetricsService) { }
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
            params.value[0] + "<br/>" +
            "ops:" + params.value[1] + "<br/>" +
            "op type:" + params.value[2] + "<br/>" +
            "start:" + params.value[3] + "<br/>" +
            "end:" + params.value[4] + "<br/>" +
            "duration:" + params.value[5] + "<br/>" +
            "implementation:" + params.value[6] + "<br/>" +
            "qty:" + params.value[7] + "<br/>" +
            "worker:" + params.value[8]
          );
        },
        axisPointer: {
          animation: false,
        },
      },
      xAxis: {
        type: 'category',
        data: Array.from({length: MetricsService.MAX_STATS}, (_, i) => i + 1)
      },
      yAxis: {
        type: 'value',
        boundaryGap: [0, '100%'],
      },
      series: [
        {
          type: 'line',
          showSymbol: false,
        },
      ],
    };
    this.latency = {
      title: {
        text: 'Latency (ms)',
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          params = params[0];

          return (
            params.value[0] + "<br/>" +
            "min:" + params.value[2] + "<br/>" +
            "avg:" + params.value[3] + "<br/>" +
            "max:" + params.value[4] + "<br/>" +
            "op type:" + params.value[5] + "<br/>" +
            "start:" + params.value[6] + "<br/>" +
            "end:" + params.value[7] + "<br/>" +
            "duration:" + params.value[8] + "<br/>" +
            "implementation:" + params.value[9] + "<br/>" +
            "qty:" + params.value[10] + "<br/>" +
            "worker:" + params.value[11]
          );
        },
        axisPointer: {
          animation: false,
        },
      },
      xAxis: {
        type: 'category',
        data: Array.from({length: MetricsService.MAX_STATS}, (_, i) => i + 1)
      },
      yAxis: {
        type: 'value',
        boundaryGap: [0, '100%'],
      },
      series: [
        {
          name: 'avg',
          type: 'line',
          showSymbol: false,
        }
      ],
    };
    MetricsService.update$.subscribe((stat: Stat<any>) => {
      this.throughputUpdate = {
        series: [
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i, s.ops, this.utilityService.enumValueToKey(WorkloadType, WorkloadType.WRITE) == s.workload.type ? s.workload.opType : this.utilityService.enumValueToKey(WorkloadType, WorkloadType.READ), s.startAt, s.endAt, s.duration, s.workload.impl, s.workload.qty, s.workload.numWorkers] } })
          },
        ],
      };
      this.latencyUpdate = {
        series: [
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i, s.avg, s.min, s.avg, s.max, this.utilityService.enumValueToKey(WorkloadType, WorkloadType.WRITE) == s.workload.type ? s.workload.opType : this.utilityService.enumValueToKey(WorkloadType, WorkloadType.READ), s.startAt, s.endAt, s.duration, s.workload.impl, s.workload.qty, s.workload.numWorkers] } })
          },
          {
            data: this.metricsService.getResults().map((s, i) => { return { name: "" + i + 1, value: [i, s.avg, s.min, s.avg, s.max, this.utilityService.enumValueToKey(WorkloadType, WorkloadType.WRITE) == s.workload.type ? s.workload.opType : this.utilityService.enumValueToKey(WorkloadType, WorkloadType.READ), s.startAt, s.endAt, s.duration, s.workload.impl, s.workload.qty, s.workload.numWorkers] } })
          },
        ],
      };
    });
  }
}

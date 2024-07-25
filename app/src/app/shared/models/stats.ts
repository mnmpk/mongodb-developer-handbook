import { Workload } from "./workload";

export class Stat<T> {
    startAt!:Date;
    endAt!:Date;
    workload!:Workload;
    duration!:number;
    ops!:number;
    min!:number;
    max!:number;
    avg!:number;
    data!:T[];
}
export class Page<T> {
    content!: T[];
    size!: number;
    totalElements!: number;
    totalPages!: number;
    empty!: boolean;
    first!: boolean;
    last!: boolean;
    number!: number;
    numberOfElements!: number
}
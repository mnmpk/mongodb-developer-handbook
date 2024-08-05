export class Page<T> {
    content!: T[];
    page!: {
        size: number,
        number: number,
        totalElements: number,
        totalPages: number,
        empty: boolean,
        first: boolean,
        last: boolean,
        numberOfElements: number
    };
}
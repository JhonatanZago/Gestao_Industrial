import { Observable, Subject, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';

export class QueryCoordinator<T, R> {
  private readonly queries = new Subject<T>();
  readonly results: Observable<R>;
  constructor(request: (query: T) => Observable<R>, delayMs = 250) {
    this.results = this.queries.pipe(debounceTime(delayMs), distinctUntilChanged(), switchMap(request));
  }
  next(query: T) { this.queries.next(query); }
}

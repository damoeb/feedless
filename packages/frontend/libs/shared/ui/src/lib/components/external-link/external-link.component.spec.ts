import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalLinkComponent } from './external-link.component';
import { AppTestModule } from '@feedless/testing';

describe('ExternalLinkComponent', () => {
  let component: ExternalLinkComponent;
  let fixture: ComponentFixture<ExternalLinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExternalLinkComponent, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ExternalLinkComponent);

    const componentRef = fixture.componentRef;
    componentRef.setInput('href', 'https://localhost/some-url');

    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.debugElement.nativeElement.innerHTML).toEqual(
      '<a itemprop="url" rel="nofollow" href="https://localhost/some-url"></a>',
    );
  });
});

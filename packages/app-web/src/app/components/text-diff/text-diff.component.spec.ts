import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextDiffComponent } from './text-diff.component';
import { AppTestModule } from '../../app-test.module';

describe('TextDiffComponent', () => {
  let component: TextDiffComponent;
  let fixture: ComponentFixture<TextDiffComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TextDiffComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TextDiffComponent);
    component = fixture.componentInstance;
  });

  function render(inputs: { before: string; after?: string; editable?: boolean }) {
    Object.entries(inputs).forEach(([key, value]) => fixture.componentRef.setInput(key, value));
    fixture.detectChanges();
  }

  it('should create', () => {
    render({ before: '' });
    expect(component).toBeTruthy();
  });

  it('reports an inserted line as a single change', () => {
    render({ before: 'a\nb\nc\nd', after: 'a\ninserted\nb\nc\nd' });

    const chunks = component.getChunks();
    expect(chunks.length).toBe(1);
    expect(chunks[0].toB - chunks[0].fromB).toBe('inserted\n'.length);
  });

  it('reports no change when after is omitted', () => {
    render({ before: 'a\nb' });

    expect(component.getChunks().length).toBe(0);
  });

  it('emits edits of the after side when editable', () => {
    render({ before: 'a', after: 'a', editable: true });
    const changes: string[] = [];
    component.afterChange.subscribe((text) => changes.push(text));

    component.getAfterView().dispatch({ changes: { from: 1, insert: 'b' } });

    expect(changes).toEqual(['ab']);
  });

  it('keeps the after side read-only by default', () => {
    render({ before: 'a', after: 'a' });

    expect(component.getAfterView().state.readOnly).toBe(true);
  });
});

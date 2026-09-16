import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  input,
  OnChanges,
  OnDestroy,
  output,
  viewChild,
} from '@angular/core';
import { Chunk, MergeView } from '@codemirror/merge';
import { EditorState, Extension } from '@codemirror/state';
import { EditorView, lineNumbers } from '@codemirror/view';

@Component({
  selector: 'app-text-diff',
  templateUrl: './text-diff.component.html',
  styleUrls: ['./text-diff.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
})
export class TextDiffComponent implements AfterViewInit, OnChanges, OnDestroy {
  readonly before = input.required<string>();
  readonly after = input<string>();
  readonly editable = input(false);

  readonly afterChange = output<string>();

  private readonly container = viewChild.required<ElementRef<HTMLDivElement>>('container');
  private mergeView: MergeView;

  ngAfterViewInit() {
    this.render();
  }

  ngOnChanges() {
    if (this.mergeView) {
      this.render();
    }
  }

  ngOnDestroy() {
    this.mergeView?.destroy();
  }

  getChunks(): readonly Chunk[] {
    return this.mergeView.chunks;
  }

  getAfterView(): EditorView {
    return this.mergeView.b;
  }

  private render() {
    this.mergeView?.destroy();
    const before = this.before() ?? '';
    const editable = this.editable();
    const shared: Extension[] = [lineNumbers()];

    this.mergeView = new MergeView({
      a: {
        doc: before,
        extensions: [...shared, EditorState.readOnly.of(true), EditorView.editable.of(false)],
      },
      b: {
        doc: this.after() ?? before,
        extensions: [
          ...shared,
          EditorState.readOnly.of(!editable),
          EditorView.editable.of(editable),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              this.afterChange.emit(update.state.doc.toString());
            }
          }),
        ],
      },
      parent: this.container().nativeElement,
      revertControls: editable ? 'a-to-b' : undefined,
      collapseUnchanged: { margin: 3, minSize: 8 },
    });
  }
}

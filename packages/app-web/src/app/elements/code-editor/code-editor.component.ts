import {
  AfterViewInit,
  Component,
  ElementRef,
  forwardRef,
  inject,
  Injector,
  input,
  OnChanges,
  output,
  SimpleChanges,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';

import { EditorState, Extension, StateField } from '@codemirror/state';
import {
  drawSelection,
  EditorView,
  gutter,
  highlightActiveLine,
  highlightSpecialChars,
  keymap,
  lineNumbers,
  showTooltip,
  Tooltip,
} from '@codemirror/view';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';
import {
  bracketMatching,
  foldGutter,
  foldKeymap,
  indentOnInput,
  syntaxTree,
} from '@codemirror/language';
import { highlightSelectionMatches } from '@codemirror/search';
import {
  autocompletion,
  closeBrackets,
  closeBracketsKeymap,
  Completion,
  CompletionContext,
  completionKeymap,
  CompletionResult,
  startCompletion,
} from '@codemirror/autocomplete';
import { codeEditorTheme } from './code-editor.theme';
import { hashtagMatcher } from './hashtag.widget';
import { urlDecorator } from './url.decorator';
import { inlineImagePlugin } from './inline-image.widget';
import { IterMode } from '@lezer/common';
import { addLineHighlight, lineHighlightField } from './line.decorator';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { ControlValueAccessorDirective } from '../../directives/control-value-accessor/control-value-accessor.directive';
import { addIcons } from 'ionicons';
import { linkOutline, listOutline } from 'ionicons/icons';
import { NgClass } from '@angular/common';
import { checkboxDecorator } from './checkbox.decorator';
import { decorateFirstLine } from './headline.decorator';
import { decorateBlockquote } from './blockquote.decorator';
import { decorateEmptyActiveLine } from './empty-active-line.decorator';

function getCursorTooltips(state: EditorState): readonly Tooltip[] {
  return [];
  // return state.selection.ranges
  //   .filter(range => range.empty)
  //   .map(range => {
  //     // let line = state.doc.lineAt(range.head)
  //     // let text = line.number + ":" + (range.head - line.from)
  //     const node = syntaxTree(state).resolve(range.from).node;
  //     if (['Hashtag', 'Link'].includes(node.name)) {
  //       return {
  //         pos: range.head,
  //         above: false,
  //         strictSide: true,
  //         arrow: false,
  //         create: () => {
  //           const div = document.createElement("div")
  //           div.className = "cm-tooltip-cursor"
  //           // dom.textContent = text
  //           const element = document.createElement("span");
  //           // ahref.setAttribute("href", "");
  //           element.innerText = "Alt + Enter for options"
  //
  //           div.append(element)
  //           return {dom: div}
  //         }
  //       }
  //     }
  //
  //   })
  //   .filter(t => t)
}

const cursorTooltipField = StateField.define<readonly Tooltip[]>({
  create: getCursorTooltips,

  update(tooltips, tr) {
    if (!tr.docChanged && !tr.selection) {
      return tooltips;
    }
    return getCursorTooltips(tr.state);
  },

  provide: (f) => showTooltip.computeN([f], (state) => state.field(f)),
});

export type ContentType = 'json' | 'text' | 'html' | 'markdown';

//   {label: "match", apply: 'karli'},
//   {label: "hello", info: "(World)"},
export type AutoSuggestionsProvider = (query: string, type: string) => Promise<Completion[]>;

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.scss'],
  encapsulation: ViewEncapsulation.ShadowDom,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CodeEditorComponent),
      multi: true,
    },
  ],
  imports: [NgClass],
  standalone: true,
})
export class CodeEditorComponent
  extends ControlValueAccessorDirective<string>
  implements AfterViewInit, OnChanges
{
  readonly editor = viewChild.required<ElementRef<HTMLDivElement>>('editor');

  readonly text = input<string>();

  readonly contentType = input<ContentType>();

  readonly readOnly = input<boolean>(false);

  readonly autofocus = input<boolean>(false);

  readonly highlightedLines = input<number[]>();

  readonly lineWrapping = input<boolean>(true);

  readonly lineNumbers = input<boolean>(true);

  readonly extensions = input<Extension[]>([]);

  // @Input()
  // scrollLeft: number;

  readonly textChange = output<string>();

  // @Output()
  // scrollChange = new EventEmitter<{ left: number, top: number }>();

  readonly triggerQuery = output<string>();

  readonly autoSuggestionsProvider = input<AutoSuggestionsProvider>(() => Promise.resolve([]));

  private editorView: EditorView;
  ctrlPressed: boolean;

  constructor() {
    super(inject<Injector>(Injector));
    addIcons({ listOutline, linkOutline });
  }

  ngAfterViewInit() {
    this.setText(this.text() || this.control.value);
    this.highlightLines(this.highlightedLines());
  }

  private highlightLines(lines: number[]) {
    lines?.forEach((line) => {
      const docPosition = this.editorView.state.doc.line(line).from;
      this.editorView.dispatch({ effects: addLineHighlight.of(docPosition) });
    });
  }

  private getExtensions() {
    const textChangeHook = this.textChange;

    const extensions: Extension[] = [
      gutter({
        renderEmptyElements: true,
      }),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          const text = this.getText();
          textChangeHook.emit(text);
          this.control.setValue(text);
        }
      }),
      EditorState.readOnly.of(this.readOnly()),
      highlightSpecialChars(),
      foldGutter(),
      lineHighlightField,
      decorateFirstLine,
      decorateBlockquote,
      decorateEmptyActiveLine,
      drawSelection(),
      EditorState.allowMultipleSelections.of(false),
      indentOnInput(),
      bracketMatching(),
      closeBrackets(),
      highlightActiveLine(),
      // rectangularSelection(),
      highlightSelectionMatches(),
      hashtagMatcher,
      ...this.extensions(),
      // noteReferenceMatcher,
      keymap.of([
        ...closeBracketsKeymap,
        ...defaultKeymap,
        ...historyKeymap,
        ...foldKeymap,
        ...completionKeymap,
      ] as any),
      // defaultHighlightStyle,
      cursorTooltipField.extension,
      // markdown(),
      // EditorView.updateListener.of((v) => {
      //   // console.log(v.transactions)
      //   // if (v.docChanged) {
      //   //   console.log('change', v.changes);
      //   // }
      // }),
      EditorView.domEventHandlers({
        keyup: (event, view) => {},
        click: (event, editorView) => {
          if (!event.ctrlKey) {
            return;
          }
          const ranges = editorView.state.selection.ranges;
          if (!ranges) {
            return;
          }
          const range = ranges[ranges.length - 1];
          if (range.from === range.to) {
            const node = syntaxTree(editorView.state).resolve(range.to).node;
            // todo if ([NODE_HASHTAG, NODE_LINK].includes(node.name)) {
            //   const token = node.cursor(IterMode.ExcludeBuffers);
            //   const query = editorView.state.sliceDoc(token.from, token.to);
            //   this.triggerQuery.emit(query);
            // }
          }
        },
      }),
      EditorView.domEventObservers({
        // scroll: debounce(() => {
        //   const scrollDOM = this.editorView.scrollDOM;
        //   // this.scrollChange.emit({
        //   this.scrollChange.next({
        //     left: scrollDOM.scrollLeft,
        //     top: scrollDOM.scrollTop
        //   })
        // }, 100),
        keydown: (event, editorView) => {
          const keys = ['Enter'];
          // const keys = ['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];

          if (keys.includes(event.key) && event.altKey) {
            startCompletion(editorView);
          }
        },
      }),
      codeEditorTheme,
      history({ minDepth: 100 }),
      checkboxDecorator,
      // markdownDecorator,
      urlDecorator,
      inlineImagePlugin,
      autocompletion({
        selectOnOpen: true,
        activateOnTyping: true,
        aboveCursor: false,
        closeOnBlur: false,
        override: [
          async (context: CompletionContext): Promise<CompletionResult | null> => {
            const token = context.matchBefore(/[^ ]*/).text[0];
            const node = syntaxTree(context.state).resolve(context.pos).node;
            const cursor = node.cursor(IterMode.ExcludeBuffers);
            // console.log('autocomplete', node);
            const opToken = '@';

            // if ([NODE_HASHTAG, 'Link'].includes(node.name)) {
            //   const query = context.state.sliceDoc(cursor.from, cursor.to);
            //   const options = await this.autoSuggestionsProvider()(query, node.name);
            //   return {
            //     from: cursor.from,
            //     filter: false,
            //     options,
            //   };
            // } else {
            if (token === opToken) {
              const selection = context.state.wordAt(context.pos);
              const resolveQuery = () => {
                if (selection) {
                  return context.state.sliceDoc(selection.from, selection.to);
                } else {
                  return '';
                }
              };

              const from = selection?.from || context.pos;

              const options = await this.autoSuggestionsProvider()(resolveQuery(), node.name);
              return {
                from: from - 1,
                filter: false,
                options,
              };
            }
            // }
          },
        ],
      }),
      // EditorView.updateListener.of(debounce((update: ViewUpdate) => {
      //   console.log('this.highlightedLines', this.highlightedLines)
      //   this.highlightedLines = [1, 4, 10];
      //   this.highlightedLines?.forEach(line => this.highlightLine(line))
      //
      // }, 300)),
    ];

    if (this.lineNumbers()) {
      extensions.push(lineNumbers());
    }
    if (this.lineWrapping()) {
      extensions.push(EditorView.lineWrapping);
    }
    return extensions;
  }

  setFocus() {
    this.editorView.focus();
  }

  private setText(text: string) {
    if (this.editorView) {
      this.editorView.destroy();
    }

    const state = EditorState.create({
      doc: text,
      extensions: this.getExtensions(),
    });

    const editor = this.editor();
    if (editor?.nativeElement) {
      this.editorView = new EditorView({
        state,
        parent: editor.nativeElement,
      });

      if (this.autofocus()) {
        this.setFocus();
      }
    }
  }

  getText() {
    return this.editorView.state.doc.toString();
  }

  handleCtrlUp(event: KeyboardEvent) {
    this.ctrlPressed = false;
  }

  handleCtrlDown(event: KeyboardEvent) {
    if (event.ctrlKey) {
      this.ctrlPressed = true;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.text?.currentValue) {
      this.setText(changes.text.currentValue);
    }
    // const scrollLeft = changes.scrollLeft?.currentValue;
    // if (scrollLeft && this.editorView?.scrollDOM) {
    //   console.log('scrollLeft', scrollLeft)
    //   this.editorView.scrollDOM.scrollLeft = scrollLeft;
    // }
  }
}

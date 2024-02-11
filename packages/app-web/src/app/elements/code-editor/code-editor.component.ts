import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  ViewChild,
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

import {
  markdownLanguageSupport,
  NODE_HASHTAG,
  NODE_LINK,
} from './markdown.lang';
import { theme } from './theme';
import { markdownDecorator } from './markdown.decorator';
import { hashtagMatcher } from './hashtag.widget';
import { urlDecorator } from './url.decorator';
import { lintKeymap } from '@codemirror/lint';
import { inlineImagePlugin } from './inline-image.widget';
import { checkboxPlugin } from './checkbox.widget';
import { noteReferenceMatcher } from './note-reference.widget';
import { IterMode } from '@lezer/common';

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
    if (!tr.docChanged && !tr.selection) return tooltips;
    return getCursorTooltips(tr.state);
  },

  provide: (f) => showTooltip.computeN([f], (state) => state.field(f)),
});

//   {label: "match", apply: 'karli'},
//   {label: "hello", info: "(World)"},
export type AutoSuggestionsProvider = (
  query: string,
  type: string,
) => Promise<Completion[]>;

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.scss'],
  encapsulation: ViewEncapsulation.ShadowDom,
})
export class CodeEditorComponent implements AfterViewInit {
  @ViewChild('editor')
  editor!: ElementRef<HTMLDivElement>;

  @Input({ required: true })
  text: string;

  @Input()
  readOnly: boolean = false;

  @Output()
  textChange = new EventEmitter<string>();

  @Output()
  triggerQuery = new EventEmitter<string>();

  @Input()
  autoSuggestionsProvider: AutoSuggestionsProvider = () => Promise.resolve([]);

  private editorView: EditorView;
  ctrlPressed: boolean;

  constructor() {}

  ngAfterViewInit() {
    this.setText(this.text);
  }

  private getExtensions() {
    const textChangeHook = this.textChange;
    const extensions: Extension[] = [
      gutter({
        renderEmptyElements: true,
      }),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          textChangeHook.emit(this.getText());
        }
      }),
      lineNumbers(),
      EditorState.readOnly.of(this.readOnly),
      highlightSpecialChars(),
      foldGutter(),
      drawSelection(),
      EditorState.allowMultipleSelections.of(false),
      indentOnInput(),
      bracketMatching(),
      closeBrackets(),
      highlightActiveLine(),
      // rectangularSelection(),
      highlightSelectionMatches(),
      hashtagMatcher,
      noteReferenceMatcher,
      EditorView.lineWrapping,
      keymap.of([
        ...closeBracketsKeymap,
        ...defaultKeymap,
        ...historyKeymap,
        ...foldKeymap,
        ...completionKeymap,
        ...lintKeymap,
      ]),
      // defaultHighlightStyle,
      cursorTooltipField.extension,
      markdownLanguageSupport,
      // EditorView.updateListener.of((v) => {
      //   // console.log(v.transactions)
      //   // if (v.docChanged) {
      //   //   console.log('change', v.changes);
      //   // }
      // }),
      EditorView.domEventHandlers({
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
            if ([NODE_HASHTAG, NODE_LINK].includes(node.name)) {
              const token = node.cursor(IterMode.ExcludeBuffers);
              const query = editorView.state.sliceDoc(token.from, token.to);
              this.triggerQuery.emit(query);
            }
          }
        },
      }),
      EditorView.domEventObservers({
        keydown: (event, editorView) => {
          const keys = ['Enter'];
          // const keys = ['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];

          if (keys.includes(event.key) && event.altKey) {
            startCompletion(editorView);
          }
        },
      }),
      theme,
      history({ minDepth: 10 }),
      checkboxPlugin.extension,
      markdownDecorator,
      urlDecorator,
      inlineImagePlugin,
      autocompletion({
        selectOnOpen: true,
        activateOnTyping: true,
        aboveCursor: false,
        closeOnBlur: false,
        override: [
          async (
            context: CompletionContext,
          ): Promise<CompletionResult | null> => {
            const firstToken = context.matchBefore(/[^ ]*/).text[0];
            const node = syntaxTree(context.state).resolve(context.pos).node;
            const token = node.cursor(IterMode.ExcludeBuffers);

            if ([NODE_HASHTAG, 'Link'].includes(node.name)) {
              const query = context.state.sliceDoc(token.from, token.to);
              const options = await this.autoSuggestionsProvider(query, node.name);
              return {
                from: token.from,
                filter: false,
                options,
              };
            } else {
              if (firstToken === '/') {
                const selection = context.state.wordAt(context.pos);
                function resolveQuery() {
                  if (selection) {
                    return context.state.sliceDoc(selection.from, selection.to);
                  } else {
                    return '';
                  }
                }

                const from = selection?.from || context.pos;

                const options = await this.autoSuggestionsProvider(
                  resolveQuery(),
                  node.name,
                );
                return {
                  from: firstToken === '/' ? from - 1 : from,
                  filter: false,
                  options,
                };
              }
            }
          },
        ],
      }),
      // EditorView.inputHandler.of((view, from, to, text) => {
      //   console.log('text', text);
      //   if (text === ' ') {
      //     closeCompletion(view);
      //   } else {
      //     startCompletion(view);
      //   }
      //   return false
      // })
    ];
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
    this.editorView = new EditorView({
      state,
      parent: this.editor.nativeElement,
    });
    this.setFocus();
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
}

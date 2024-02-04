import {EditorView} from "@codemirror/view";

export const theme = EditorView.theme({
  ".cm-atx-heading-1, .cm-setext-heading-1": {fontSize: "2em"},
  ".cm-atx-heading-2, .cm-setext-heading-2": {fontSize: "1.5em"},
  ".cm-atx-heading-3": {fontSize: "1.17em"},
  ".cm-atx-heading-4": {fontWeight: "bold"},
  ".cm-atx-heading-5": {fontSize: "0.83em", fontWeight: "bold"},
  ".cm-atx-heading-6": {fontSize: "0.67em", fontWeight: "bold"},
  ".cm-url": {color: "blue"},
  ".cm-open-link": {cursor: 'pointer', marginLeft: '3px', textDecoration: 'none', color:'red'},
  ".cm-emphasis": {fontStyle: 'italic'},
  ".cm-hashtag": {background: '#a8ffa5'},
  ".cm-strong-emphasis": {fontStyle: 'oblique'},
  ".cm-strikethrough": {textDecoration: 'line-through'},
  ".cm-code-text": {fontFamily: 'monospace', borderLeft: '4px solid #cccccc'},
  "[class*=\"-mark\"]": {color: '#cccccc'}
})

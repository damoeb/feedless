import { EditorView } from '@codemirror/view';

export const theme = EditorView.theme({
  '.cm-atx-heading-1, .cm-setext-heading-1': { fontSize: '2em' },
  '.cm-atx-heading-2, .cm-setext-heading-2': { fontSize: '1.5em' },
  '.cm-atx-heading-3': { fontSize: '1.17em' },
  '.cm-atx-heading-4': { fontWeight: 'bold' },
  '.cm-atx-heading-5': { fontSize: '0.83em', fontWeight: 'bold' },
  '.cm-atx-heading-6': { fontSize: '0.67em', fontWeight: 'bold' },
  '.cm-activeLine': { backgroundColor: 'rgba(var(--ion-color-primary-rgb), 0.05)' },
  '.cm-line:has(.cm-blockquote)': {
    backgroundColor: 'transparent', paddingTop: '3px',
    paddingBottom: '3px'
  },
  '.cm-url': { color: 'var(--ion-color-primary)' },
  '.cm-cursor': { borderRight: '2px solid var(--foreground)' },
  '.cm-open-link': { cursor: 'pointer', marginLeft: '3px', textDecoration: 'none', color: 'red' },
  '.cm-emphasis': { fontStyle: 'italic' },
  '.cm-line:has(.cm-fenced-code)': { fontStyle: 'monospace', backgroundColor: 'rgba(var(--ion-color-dark-rgb), 0.2)' },
  '.cm-hashtag': { background: '#a8ffa5', color: 'var(--background)' },
  '.cm-strong-emphasis': { fontStyle: 'oblique' },
  '.cm-strikethrough': { textDecoration: 'line-through' },
  '.cm-gutters': { backgroundColor: 'var(--background)', borderRight: 'none' },
  '[class*="-mark"]': { color: '#cccccc' }
});

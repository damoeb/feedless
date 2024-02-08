import { LanguageDescription, LanguageSupport } from '@codemirror/language';
import { InlineContext } from '@lezer/markdown';
import { markdown } from '@codemirror/lang-markdown';

export const NODE_HASHTAG = 'Hashtag';
export const NODE_LINK = 'Link';
export const markdownLanguageSupport = markdown({
  extensions: [
    {
      defineNodes: [NODE_HASHTAG],
      parseInline: [
        {
          name: NODE_HASHTAG,
          parse(context: InlineContext, next: number, pos: number): number {
            if (
              context.char(pos) === '#'.charCodeAt(0) &&
              context.char(pos + 1) !== 32
            ) {
              let end = pos + 1;
              while (
                end < context.end - 1 &&
                ![0, 32].includes(context.char(end))
              ) {
                end++;
              }
              return context.addElement(
                context.elt(NODE_HASHTAG, pos, end + 1),
              );
            }

            return -1;
          },
        },
      ],
    },
  ],
  codeLanguages: [
    LanguageDescription.of({
      name: 'javascript',
      alias: ['js', 'jsx'],
      async load() {
        const { javascriptLanguage } = await import(
          '@codemirror/lang-javascript'
        );
        return new LanguageSupport(javascriptLanguage);
      },
    }),
    LanguageDescription.of({
      name: 'css',
      async load() {
        const { cssLanguage } = await import('@codemirror/lang-css');
        return new LanguageSupport(cssLanguage);
      },
    }),
    LanguageDescription.of({
      name: 'json',
      async load() {
        const { jsonLanguage } = await import('@codemirror/lang-json');
        return new LanguageSupport(jsonLanguage);
      },
    }),
    LanguageDescription.of({
      name: 'html',
      alias: ['htm'],
      async load() {
        const { jsxLanguage } = await import('@codemirror/lang-javascript');
        const javascript = new LanguageSupport(jsxLanguage);
        const { cssLanguage } = await import('@codemirror/lang-css');
        const css = new LanguageSupport(cssLanguage);
        const { htmlLanguage } = await import('@codemirror/lang-html');

        return new LanguageSupport(htmlLanguage, [css, javascript]);
      },
    }),
  ],
});

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
    // LanguageDescription.of({
    //   name: 'javascript',
    //   alias: ['js', 'jsx'],
    //   load: async () => new LanguageSupport(javascriptLanguage),
    // }),
    // LanguageDescription.of({
    //   name: 'css',
    //   load: async () => new LanguageSupport(cssLanguage),
    // }),
    // LanguageDescription.of({
    //   name: 'json',
    //   load: async () => new LanguageSupport(jsonLanguage),
    // }),
    // LanguageDescription.of({
    //   name: 'html',
    //   alias: ['htm'],
    //   load: async () => {
    //     const javascript = new LanguageSupport(jsxLanguage);
    //     const css = new LanguageSupport(cssLanguage);
    //
    //     return new LanguageSupport(htmlLanguage, [css, javascript]);
    //   },
    // }),
  ],
});

import { Argument, Command } from 'commander';

const program = new Command();
program.version('0.1');

program
  .command('server')
  .description('clone a repository into a newly created directory')
  .action((url) => {
    console.log('clone command called');
  })
  .command('add')
  .addArgument(new Argument('url', 'url of rich server'))
  .description('clone a repository into a newly created directory')
  .action((url) => {
    console.log('clone command called');
  })
  .command('remove')
  .addArgument(new Argument('url', 'url of rich server'))
  .description('clone a repository into a newly created directory')
  .action((url) => {
    console.log('clone command called');
  });

program.parse(process.argv);

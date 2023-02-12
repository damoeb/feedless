function stopContainers() {
  `docker-compose down`
}
function startContainers(containerNames) {
  `docker-compose up ${containerNames.json(' ')}`
}
function startApp(profiles) {
  `gradle bootRun --args='--spring.profiles.active=${profiles.join(',')}'`
}
// const { exec } = require('node:child_process');
// exec('cat *.js | wc -l', (error, stdout, stderr) => {
//   if (error) {
//     console.error(`exec error: ${error}`);
//     return;
//   }
//   console.log(`stdout: ${stdout}`);
//   console.error(`stderr: ${stderr}`);
// });

const profiles = {
  prod: 'prod',
  dev: 'dev',
  // --
  database: 'database',
  bootstrap: 'bootstrap',
  nothrottle: 'nothrottle',
  nocache: 'nocache'
}

const containers = {
  postgres: 'postgres',
  elastic: 'elastic',
  rabbitmq: 'rabbitmq'
}

const configs = [
  {
    profiles: [],
    containers: [],
    expectedLogs: []
  },
  {
    profiles: [profiles.prod],
    containers: [],
    expectedLogs: []
  },
  {
    profiles: [profiles.database],
    containers: [containers.postgres, containers.elastic, containers.rabbitmq, profiles.prod],
    expectedLogs: []
  },
  {
    profiles: [profiles.database, profiles.bootstrap],
    containers: [containers.postgres, containers.elastic, containers.rabbitmq, profiles.prod],
    expectedLogs: []
  },
  {
    profiles: [profiles.nocache, profiles.prod],
    containers: [],
    expectedLogs: []
  },
  {
    profiles: [profiles.nothrottle, profiles.prod],
    containers: [],
    expectedLogs: []
  }
]

configs.forEach(config => {
  startContainers(config.containers)
  startApp(config.profiles)
})

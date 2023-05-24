FROM damoeb/feedless:app-$APP_APP_VERSION
RUN ls -lah

FROM damoeb/feedless:agent-$APP_AGENT_VERSION
RUN ls -lah

FROM damoeb/feedless:core-$APP_CORE_VERSION

RUN yum install nodejs18

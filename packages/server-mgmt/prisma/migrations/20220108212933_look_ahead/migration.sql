-- AlterTable
ALTER TABLE "ArticleExporter" ADD COLUMN "segment_look_ahead_min" INTEGER;

-- CreateFunction

CREATE OR REPLACE FUNCTION add_minutes(d timestamptz, diff integer) RETURNS timestamptz AS $$
BEGIN
RETURN d + diff * interval '1' MINUTE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION has_articles_between(a timestamptz, b timestamptz, feedId text) RETURNS boolean AS $$
BEGIN
SELECT 1 FROM "ArticleRef" r
inner join "Feed" F on r."streamId" = F."streamId"
where f."id" = feedId and r."createdAt" between a and b limit 1;
END
$$ LANGUAGE plpgsql;

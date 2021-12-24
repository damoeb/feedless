-- AlterTable
ALTER TABLE "Feed" ADD COLUMN     "managed" BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN     "managed_by_plugin_id" TEXT,
ALTER COLUMN "harvest_site" SET DEFAULT true;

-- CreateTable
CREATE TABLE "Plugin" (
    "id" TEXT NOT NULL,
    "source_url" VARCHAR(500) NOT NULL,
    "type" TEXT NOT NULL,
    "user_params" JSONB NOT NULL,
    "ownerId" TEXT NOT NULL DEFAULT E'system',
    "source" JSONB,
    "source_sha1" TEXT,
    "lastUpdatedAt" TIMESTAMP(3) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY ("id")
);

-- AddForeignKey
ALTER TABLE "Plugin" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Feed" ADD FOREIGN KEY ("managed_by_plugin_id") REFERENCES "Plugin"("id") ON DELETE SET NULL ON UPDATE CASCADE;

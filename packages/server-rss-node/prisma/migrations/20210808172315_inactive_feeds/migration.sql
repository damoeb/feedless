/*
  Warnings:

  - You are about to drop the column `description` on the `Subscription` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `Feed` ADD COLUMN `inactive` BOOLEAN NOT NULL DEFAULT false;

-- AlterTable
ALTER TABLE `Subscription` DROP COLUMN `description`,
    ADD COLUMN `inactive` BOOLEAN NOT NULL DEFAULT false;


-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."native";
CREATE SEQUENCE "public"."native"
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for app_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."app_info";
CREATE TABLE "public"."app_info" (
  "id" int8 NOT NULL,
  "app_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "current_server" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "password" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;


-- ----------------------------
-- Table structure for container_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."container_info";
CREATE TABLE "public"."container_info" (
  "id" int8 NOT NULL,
  "app_id" int8,
  "container_tag" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "container_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "container_desc" varchar(1024) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "container_exec_path" varchar(1024) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "last_deploy_time" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "source_info" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "source_type" int4,
  "status" int4,
  "version" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "extra" varchar(1024) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;
COMMENT ON COLUMN "public"."container_info"."source_type" IS '1:fatJar 2:git 3:script';

-- ----------------------------
-- Table structure for instance_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."instance_info";
CREATE TABLE "public"."instance_info" (
  "id" int8 NOT NULL,
  "actual_trigger_time" int8,
  "app_id" int8,
  "expected_trigger_time" int8,
  "finished_time" int8,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "instance_id" int8,
  "instance_params" text COLLATE "pg_catalog"."default",
  "job_id" int8,
  "last_report_time" int8,
  "result" text COLLATE "pg_catalog"."default",
  "running_times" int8,
  "status" int4,
  "task_tracker_address" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "type" int4,
  "wf_instance_id" int8,
  "active" int4,
  "job_params" text COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for job_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."job_info";
CREATE TABLE "public"."job_info" (
  "id" int8 NOT NULL,
  "app_id" int8,
  "concurrency" int4,
  "designated_workers" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "execute_type" int4,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "instance_retry_num" int4,
  "instance_time_limit" int8,
  "job_description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "job_params" varchar(4096) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "max_instance_num" int4,
  "max_worker_count" int4,
  "min_cpu_cores" float8 NOT NULL,
  "min_disk_space" float8 NOT NULL,
  "min_memory_space" float8 NOT NULL,
  "next_trigger_time" int8,
  "notify_user_ids" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "processor_info" text COLLATE "pg_catalog"."default",
  "processor_type" int4,
  "container_script" int8,
  "container_config" int8,
  "status" int4,
  "task_retry_num" int4,
  "time_expression" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "time_expression_type" int4,
  "dispatch_strategy" int4,
  "extra" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "lifecycle" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "template_code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for oms_lock
-- ----------------------------
DROP TABLE IF EXISTS "public"."oms_lock";
CREATE TABLE "public"."oms_lock" (
  "id" int8 NOT NULL,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "lock_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "max_lock_time" int8,
  "ownerip" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for server_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."server_info";
CREATE TABLE "public"."server_info" (
  "id" int8 NOT NULL,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "ip" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------
-- ----------------------------
-- Table structure for template
-- ----------------------------
DROP TABLE IF EXISTS "public"."template";
CREATE TABLE "public"."template" (
  "id" int8 NOT NULL,
  "app_id" int8,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "json" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "code" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Records of template
-- ----------------------------
INSERT INTO "public"."template" VALUES (32, 5, NULL, '2022-04-15 10:55:26.464', '{\n    \"farm_name\": [\n        \"风场名称列表\"\n    ],\n    \"model_type\": \"模板类型标签\",\n    \"start_date\": \"数据开始日期\",\n    \"end_date\": \"数据结束日期\",\n    \"device_number\": [\n        \"设备位号列表\"\n    ]\n}', '金风机组参数配置模板（CMS版）', '0001');
INSERT INTO "public"."template" VALUES (34, 5, '2022-04-15 10:55:42.935', '2022-04-15 10:55:42.935', '{}', '金风机组参数配置模板（大数据平台版）', '0002');

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_info";
CREATE TABLE "public"."user_info" (
  "id" int8 NOT NULL,
  "email" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "extra" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "password" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "phone" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "username" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "web_hook" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for workflow_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."workflow_info";
CREATE TABLE "public"."workflow_info" (
  "id" int8 NOT NULL,
  "app_id" int8,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "max_wf_instance_num" int4,
  "next_trigger_time" int8,
  "notify_user_ids" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "pedag" text COLLATE "pg_catalog"."default",
  "status" int4,
  "time_expression" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "time_expression_type" int4,
  "wf_description" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "wf_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "extra" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "lifecycle" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying
)
;

-- ----------------------------
-- Table structure for workflow_instance_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."workflow_instance_info";
CREATE TABLE "public"."workflow_instance_info" (
  "id" int8 NOT NULL,
  "actual_trigger_time" int8,
  "app_id" int8,
  "dag" text COLLATE "pg_catalog"."default",
  "expected_trigger_time" int8,
  "finished_time" int8,
  "gmt_create" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "gmt_modified" timestamp(6) DEFAULT NULL::timestamp without time zone,
  "result" text COLLATE "pg_catalog"."default",
  "status" int4,
  "wf_init_params" text COLLATE "pg_catalog"."default",
  "wf_instance_id" int8,
  "workflow_id" int8,
  "wf_context" text COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for workflow_node_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."workflow_node_info";
CREATE TABLE "public"."workflow_node_info" (
  "id" int8 NOT NULL,
  "app_id" int8 NOT NULL,
  "extra" text COLLATE "pg_catalog"."default",
  "gmt_create" timestamp(6) NOT NULL,
  "gmt_modified" timestamp(6) NOT NULL,
  "job_id" int8,
  "node_name" varchar(255) COLLATE "pg_catalog"."default" DEFAULT NULL::character varying,
  "node_params" text COLLATE "pg_catalog"."default",
  "type" int4,
  "workflow_id" int8,
  "enable" bool NOT NULL,
  "skip_when_failed" bool NOT NULL
)
;

-- --
-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."native"', 424, true);

-- ----------------------------
-- Indexes structure for table app_info
-- ----------------------------
CREATE INDEX "appNameUK" ON "public"."app_info" USING btree (
  "app_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table app_info
-- ----------------------------
ALTER TABLE "public"."app_info" ADD CONSTRAINT "app_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table container_info
-- ----------------------------
CREATE INDEX "IDX8hixyaktlnwil2w9up6b0p898" ON "public"."container_info" USING btree (
  "app_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table container_info
-- ----------------------------
ALTER TABLE "public"."container_info" ADD CONSTRAINT "container_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table instance_info
-- ----------------------------
CREATE INDEX "IDX5b1nhpe5je7gc5s1ur200njr7" ON "public"."instance_info" USING btree (
  "job_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "IDXa98hq3yu0l863wuotdjl7noum" ON "public"."instance_info" USING btree (
  "instance_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "IDXjnji5lrr195kswk6f7mfhinrs" ON "public"."instance_info" USING btree (
  "app_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table instance_info
-- ----------------------------
ALTER TABLE "public"."instance_info" ADD CONSTRAINT "instance_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table job_info
-- ----------------------------
CREATE INDEX "IDXk2xprmn3lldmlcb52i36udll1" ON "public"."job_info" USING btree (
  "app_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table job_info
-- ----------------------------
ALTER TABLE "public"."job_info" ADD CONSTRAINT "job_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table oms_lock
-- ----------------------------
CREATE INDEX "lockNameUK" ON "public"."oms_lock" USING btree (
  "lock_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table oms_lock
-- ----------------------------
ALTER TABLE "public"."oms_lock" ADD CONSTRAINT "oms_lock_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table server_info
-- ----------------------------
CREATE INDEX "UKtk8ytgpl7mpukhnvhbl82kgvy" ON "public"."server_info" USING btree (
  "ip" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table server_info
-- ----------------------------
ALTER TABLE "public"."server_info" ADD CONSTRAINT "server_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table template
-- ----------------------------
CREATE INDEX "IDX15x797vme8ti3gsxymf9omna" ON "public"."template" USING btree (
  "app_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table template
-- ----------------------------
ALTER TABLE "public"."template" ADD CONSTRAINT "template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table user_info
-- ----------------------------
ALTER TABLE "public"."user_info" ADD CONSTRAINT "user_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table workflow_info
-- ----------------------------
CREATE INDEX "IDX7uo5w0e3beeho3fnx9t7eiol3" ON "public"."workflow_info" USING btree (
  "app_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table workflow_info
-- ----------------------------
ALTER TABLE "public"."workflow_info" ADD CONSTRAINT "workflow_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table workflow_instance_info
-- ----------------------------
ALTER TABLE "public"."workflow_instance_info" ADD CONSTRAINT "workflow_instance_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table workflow_node_info
-- ----------------------------
CREATE INDEX "IDX36t7rhj4mkg2a5pb4ttorscta" ON "public"."workflow_node_info" USING btree (
  "app_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "IDXacr0i6my8jr002ou8i1gmygju" ON "public"."workflow_node_info" USING btree (
  "workflow_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table workflow_node_info
-- ----------------------------
ALTER TABLE "public"."workflow_node_info" ADD CONSTRAINT "workflow_node_info_pkey" PRIMARY KEY ("id");

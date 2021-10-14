CREATE KEYSPACE IF NOT EXISTS sunbird_courses WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '1'
};


CREATE TABLE sunbird_courses.user_content_consumption_new (
    userid text,
    contentid text,
    collectionid text,
    contextid text,
    datetime timestamp,
    last_access_time timestamp,
    last_completed_time timestamp,
    last_updated_time timestamp,
    progress int,
    status int,
    PRIMARY KEY (userid, contentid, collectionid, contextid)
) WITH CLUSTERING ORDER BY (contentid ASC,collectionid ASC, contextid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

INSERT INTO sunbird_courses.user_content_consumption_new  (userid,contentid,collectionid,contextid,progress,status) VALUES ('u2', 'c2','cc2','b2',20,1);


CREATE TABLE sunbird_courses.user_enrolments (
    userid text,
    courseid text,
    batchid text,
    active boolean,
    addedby text,
    certificates list<frozen<map<text, text>>>,
    certstatus int,
    completedon timestamp,
    completionpercentage int,
    contentstatus map<text, int>,
    datetime timestamp,
    enrolled_date timestamp,
    enrolleddate text,
    issued_certificates list<frozen<map<text, text>>>,
    lastreadcontentid text,
    lastreadcontentstatus int,
    progress int,
    status int,
    PRIMARY KEY (userid, courseid, batchid)
) WITH CLUSTERING ORDER BY (courseid ASC, batchid ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';
    
CREATE TABLE sunbird_courses.user_activity_agg (
    activity_type text,
    activity_id text,
    user_id text,
    context_id text,
    content_status frozen<map<text, int>>,
    agg map<text, int>,
    agg_last_updated map<text, timestamp>,
    PRIMARY KEY ((activity_type, activity_id, user_id), context_id)
) WITH CLUSTERING ORDER BY (context_id ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

INSERT INTO sunbird_courses.user_enrolments (userid,courseid,batchid) VALUES ('u2','cc2','b2');

INSERT INTO sunbird_courses.user_enrolments (userid,courseid,batchid,active,enrolled_date,completedon,issued_certificates,progress,status) VALUES ('user01','cc01','b01',true,1633698007000,1633698007001,[{'identifier':'59f3d42b-d816-4382-ae43-b19da4fafa26','lastIssuedOn':'2021-08-03T11:36:24.796+0000','name':'Course Completion Certificate','token':'N5Q4Z7'}],100,2);

INSERT INTO sunbird_courses.user_activity_agg (activity_type,activity_id,user_id,context_id,content_status,agg) VALUES ('Course', 'cc01', 'user01', 'cb:b01', {'do_123': 2}, {'score:do_123': 10, 'max_score': 10})
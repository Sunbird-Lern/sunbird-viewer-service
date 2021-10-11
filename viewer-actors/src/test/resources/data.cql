CREATE KEYSPACE IF NOT EXISTS sunbird_courses WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '1'
};


CREATE TABLE IF NOT EXISTS sunbird_courses.user_content_consumption_new (
    userid text,
    contentid text,
    collectionid text,
    contextid text,
    last_access_time timestamp,
    last_completed_time timestamp,
    last_updated_time timestamp,
    progressdetails text,
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

INSERT INTO sunbird_courses.user_content_consumption_new  (userid,contentid,collectionid,contextid,progressdetails,status)
VALUES ('u2', 'c2','cc2','b2','{"mimetype": "video","progress":10}',1);
INSERT INTO sunbird_courses.user_content_consumption_new  (userid,contentid,collectionid,contextid,progressdetails,status)
VALUES ('user_test1', 'content_test1','content_test1','content_test1','{"mimetype": "video","progress":10}',1);


CREATE TABLE IF NOT EXISTS sunbird_courses.user_enrolments (
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

INSERT INTO sunbird_courses.user_enrolments (userid,courseid,batchid) VALUES ('u2','cc2','b2');


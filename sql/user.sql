create table user
(
    id              int auto_increment comment '用户id'
        primary key,
    user_name       varchar(256)                       null comment '用户名',
    user_account    varchar(256)                       not null comment '账户名',
    password        varchar(512)                       not null comment '密码',
    avatar_url      varchar(1024)                      null comment '头像url',
    gender          tinyint                            null comment '性别',
    email           varchar(512)                       null comment '电子邮箱',
    phone           varchar(128)                       null comment '手机号码',
    grade           smallint default 1                 not null comment '等级',
    exp             int      default 0                 not null comment '经验',
    sign_in         datetime                           null comment '上一次签到时间',
    state           tinyint  default 0                 not null comment '用户状态',
    unblocking_time datetime                           null comment '解封时间',
    is_delete       tinyint  default 0                 not null comment '逻辑删除',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建账号时间',
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户信息';

INSERT INTO gasen.user (id, user_name, user_account, password, avatar_url, gender, email, phone, grade, exp, sign_in, state, unblocking_time, is_delete, create_time, update_time) VALUES (1, null, 'test01', 'd02c77b7b10e5349c50d83898cdac269', null, null, null, null, 4, 16, '2024-02-25 23:39:56', 1, null, 0, '2024-02-23 17:12:31', '2024-02-25 23:39:55');
INSERT INTO gasen.user (id, user_name, user_account, password, avatar_url, gender, email, phone, grade, exp, sign_in, state, unblocking_time, is_delete, create_time, update_time) VALUES (2, '111', 'test02', 'd02c77b7b10e5349c50d83898cdac269', null, null, null, null, 1, 0, '2024-02-25 19:12:59', 0, '2024-02-26 19:12:37', 1, '2024-02-25 18:28:44', '2024-02-25 23:42:25');
INSERT INTO gasen.user (id, user_name, user_account, password, avatar_url, gender, email, phone, grade, exp, sign_in, state, unblocking_time, is_delete, create_time, update_time) VALUES (3, null, 'test03', 'd02c77b7b10e5349c50d83898cdac269', null, null, null, null, 1, 0, '2024-02-25 23:06:37', 0, null, 1, '2024-02-25 18:30:15', '2024-02-25 23:27:43');

create table team
(
    id          int auto_increment comment 'id'
        primary key,
    create_user int                                not null comment '创建者id',
    name        varchar(256)                       null comment '队伍名称',
    max_num     int      default 1                 not null comment '人员数目',
    description varchar(1024)                      null comment '描述',
    state       tinyint  default 0                 not null comment '(0公开，1私有，2加密) ',
    password    varchar(512)                       null comment '加密密码',
    is_delete   tinyint  default 0                 not null comment '逻辑删除',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '队伍表';


create table user_team
(
    id          int auto_increment comment 'id'
        primary key,
    user_id     int                                not null comment '用户id',
    team_id     int                                not null comment '队伍id',
    is_delete   int      default 0                 not null comment '逻辑删除',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户队伍表';

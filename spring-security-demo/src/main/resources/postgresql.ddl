create table batch_job_instance
(
    job_instance_id numeric(19) not null
        constraint con41700929
            primary key,
    version numeric(19),
    job_name varchar(100) not null,
    job_key varchar(32) not null
);

alter table batch_job_instance owner to postgres;

create unique index job_inst_un
	on batch_job_instance (job_name, job_key);

create table batch_job_execution
(
    job_execution_id numeric(19) not null
        constraint con42200155
            primary key,
    version numeric(19),
    job_instance_id numeric(19) not null
        constraint job_inst_exec_fk
            references batch_job_instance
            on update cascade on delete restrict,
    create_time timestamp not null,
    start_time timestamp,
    end_time timestamp,
    status varchar(10),
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    job_configuration_location varchar(2500)
);

alter table batch_job_execution owner to postgres;

create table batch_job_execution_context
(
    job_execution_id numeric(19) not null
        constraint con44300462
            primary key
        constraint job_exec_ctx_fk
            references batch_job_execution
            on update cascade on delete restrict,
    short_context varchar(2500) not null,
    serialized_context text
);

alter table batch_job_execution_context owner to postgres;

create table batch_job_execution_params
(
    job_execution_id numeric(19) not null
        constraint job_exec_params_fk
            references batch_job_execution
            on update cascade on delete restrict,
    type_cd varchar(6) not null,
    key_name varchar(100) not null,
    string_val varchar(250),
    date_val timestamp,
    long_val numeric(19),
    double_val numeric,
    identifying char not null
);

alter table batch_job_execution_params owner to postgres;

create table batch_step_execution
(
    step_execution_id numeric(19) not null
        constraint con43200139
            primary key,
    version numeric(19) not null,
    step_name varchar(100) not null,
    job_execution_id numeric(19) not null
        constraint job_exec_step_fk
            references batch_job_execution
            on update cascade on delete restrict,
    start_time timestamp not null,
    end_time timestamp,
    status varchar(10),
    commit_count numeric(19),
    read_count numeric(19),
    filter_count numeric(19),
    write_count numeric(19),
    read_skip_count numeric(19),
    write_skip_count numeric(19),
    process_skip_count numeric(19),
    rollback_count numeric(19),
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp
);

alter table batch_step_execution owner to postgres;

create table batch_step_execution_context
(
    step_execution_id numeric(19) not null
        constraint con43900296
            primary key
        constraint step_exec_ctx_fk
            references batch_step_execution
            on update cascade on delete restrict,
    short_context varchar(2500) not null,
    serialized_context text
);

alter table batch_step_execution_context owner to postgres;

create table qrtz_job_details
(
    sched_name varchar(120) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250),
    job_class_name varchar(250) not null,
    is_durable varchar(1) not null,
    is_nonconcurrent varchar(1) not null,
    is_update_data varchar(1) not null,
    requests_recovery varchar(1) not null,
    job_data text,
    constraint qrtz_job_details_pk
        primary key (sched_name, job_name, job_group)
);

alter table qrtz_job_details owner to postgres;

create index idx_qrtz_j_grp
	on qrtz_job_details (sched_name, job_group);

create index idx_qrtz_j_req_recovery
	on qrtz_job_details (sched_name, requests_recovery);

create table qrtz_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250),
    next_fire_time numeric(19),
    prev_fire_time numeric(19),
    priority numeric(13),
    trigger_state varchar(16) not null,
    trigger_type varchar(8) not null,
    start_time numeric(19) not null,
    end_time numeric(19),
    calendar_name varchar(200),
    misfire_instr numeric(2),
    job_data text,
    constraint qrtz_triggers_pk
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_trigger_to_jobs_fk
        foreign key (sched_name, job_name, job_group) references qrtz_job_details
            on update cascade on delete restrict
);

alter table qrtz_triggers owner to postgres;

create index idx_qrtz_t_c
	on qrtz_triggers (sched_name, calendar_name);

create index idx_qrtz_t_g
	on qrtz_triggers (sched_name, trigger_group);

create index idx_qrtz_t_j
	on qrtz_triggers (sched_name, job_name, job_group);

create index idx_qrtz_t_jg
	on qrtz_triggers (sched_name, job_group);

create index idx_qrtz_t_n_g_state
	on qrtz_triggers (sched_name, trigger_group, trigger_state);

create index idx_qrtz_t_n_state
	on qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);

create index idx_qrtz_t_next_fire_time
	on qrtz_triggers (sched_name, next_fire_time);

create index idx_qrtz_t_nft_misfire
	on qrtz_triggers (sched_name, misfire_instr, next_fire_time);

create index idx_qrtz_t_nft_st
	on qrtz_triggers (sched_name, trigger_state, next_fire_time);

create index idx_qrtz_t_nft_st_misfire
	on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);

create index idx_qrtz_t_nft_st_misfire_grp
	on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

create index idx_qrtz_t_state
	on qrtz_triggers (sched_name, trigger_state);

create table qrtz_calendars
(
    sched_name varchar(120) not null,
    calendar_name varchar(200) not null,
    calendar text not null,
    constraint qrtz_calendars_pk
        primary key (sched_name, calendar_name)
);

alter table qrtz_calendars owner to postgres;

create table qrtz_cron_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    cron_expression varchar(120) not null,
    time_zone_id varchar(80),
    constraint qrtz_cron_trig_pk
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_cron_trig_to_trig_fk
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
            on update cascade on delete restrict
);

alter table qrtz_cron_triggers owner to postgres;

create table qrtz_fired_triggers
(
    sched_name varchar(120) not null,
    entry_id varchar(140) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    instance_name varchar(200) not null,
    fired_time numeric(19) not null,
    sched_time numeric(19) not null,
    priority numeric(13) not null,
    state varchar(16) not null,
    job_name varchar(200),
    job_group varchar(200),
    is_nonconcurrent varchar(1),
    requests_recovery varchar(1),
    constraint qrtz_fired_trigger_pk
        primary key (sched_name, entry_id)
);

alter table qrtz_fired_triggers owner to postgres;

create index idx_qrtz_ft_inst_job_req_rcvry
	on qrtz_fired_triggers (sched_name, instance_name, requests_recovery);

create index idx_qrtz_ft_j_g
	on qrtz_fired_triggers (sched_name, job_name, job_group);

create index idx_qrtz_ft_jg
	on qrtz_fired_triggers (sched_name, job_group);

create index idx_qrtz_ft_t_g
	on qrtz_fired_triggers (sched_name, trigger_name, trigger_group);

create index idx_qrtz_ft_tg
	on qrtz_fired_triggers (sched_name, trigger_group);

create index idx_qrtz_ft_trig_inst_name
	on qrtz_fired_triggers (sched_name, instance_name);

create table qrtz_locks
(
    sched_name varchar(120) not null,
    lock_name varchar(40) not null,
    constraint qrtz_locks_pk
        primary key (sched_name, lock_name)
);

alter table qrtz_locks owner to postgres;

create table qrtz_paused_trigger_grps
(
    sched_name varchar(120) not null,
    trigger_group varchar(200) not null,
    constraint qrtz_paused_trig_grps_pk
        primary key (sched_name, trigger_group)
);

alter table qrtz_paused_trigger_grps owner to postgres;

create table qrtz_scheduler_state
(
    sched_name varchar(120) not null,
    instance_name varchar(200) not null,
    last_checkin_time numeric(19) not null,
    checkin_interval numeric(13) not null,
    constraint qrtz_scheduler_state_pk
        primary key (sched_name, instance_name)
);

alter table qrtz_scheduler_state owner to postgres;

create table qrtz_simple_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    repeat_count numeric(7) not null,
    repeat_interval numeric(12) not null,
    times_triggered numeric(10) not null,
    constraint qrtz_simple_trig_pk
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simple_trig_to_trig_fk
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
            on update cascade on delete restrict
);

alter table qrtz_simple_triggers owner to postgres;

create table qrtz_simprop_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    str_prop_1 varchar(512),
    str_prop_2 varchar(512),
    str_prop_3 varchar(512),
    int_prop_1 numeric(10),
    int_prop_2 numeric(10),
    long_prop_1 numeric(19),
    long_prop_2 numeric(19),
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 varchar(1),
    bool_prop_2 varchar(1),
    time_zone_id varchar(80),
    constraint qrtz_simprop_trig_pk
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simprop_trig_to_trig_fk
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
            on update cascade on delete restrict
);

alter table qrtz_simprop_triggers owner to postgres;

create table qrtz_text_triggers
(
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    text_data text,
    constraint qrtz_text_trig_pk
        primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_text_trig_to_trig_fk
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
            on update cascade on delete restrict
);

alter table qrtz_text_triggers owner to postgres;

create table sf_at_dataset
(
    dataset_id varchar(255) not null
        constraint sf_at_dataset_pkey
            primary key,
    chg_stm timestamp,
    chg_usr_id varchar(255),
    dataset_nm varchar(255),
    expln_ctnt varchar(255),
    reg_stm timestamp,
    reg_usr_id varchar(255),
    ruleset_id varchar(8),
    rule_tp_clc varchar(1),
    dataset_clc varchar(1) not null
);

alter table sf_at_dataset owner to postgres;

create table sf_at_dataset_var
(
    dataset_id varchar(10) not null,
    dataset_clc varchar(1) not null,
    var_seq numeric(3) not null,
    var_id varchar(50),
    use_tp_clc varchar(2),
    data_tp_clc varchar(2),
    var_tp_clc varchar(2),
    null_rpcm_chr_ctnt varchar(10),
    spcl_val_ctnt varchar(10),
    clc_ctnt varchar(1000),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_at_dataset_var_pk
        primary key (dataset_id, var_seq)
);

alter table sf_at_dataset_var owner to postgres;

create table sf_at_mopm_anlt_rst
(
    crt_dt varchar(8) not null,
    tgt_clc varchar(2) not null,
    tgt_id varchar(30) not null,
    raw_ctnt text,
    anlt_ctnt text,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_mopm_anlt_rst_pk
        primary key (crt_dt, tgt_clc, tgt_id)
);

alter table sf_at_mopm_anlt_rst owner to postgres;

create table sf_at_mopm_dic
(
    dic_clc varchar(1) not null,
    dic_no varchar(10) not null,
    rpst_dic_no varchar(10),
    wrd_nm varchar(50) not null,
    expln_ctnt varchar(100),
    use_yn varchar(1) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_mopm_dic_pk
        primary key (dic_clc, dic_no)
);

alter table sf_at_mopm_dic owner to postgres;

create table sf_at_mopm_rnk_agg_rst
(
    req_dt varchar(8) not null,
    req_seq numeric(3) not null,
    tgt_clc varchar(2) not null,
    prcs_seq numeric(3) not null,
    rnk_seq numeric(3),
    mopm_nm varchar(50) not null,
    mopm_cnt numeric(10) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_mopm_rnk_agg_rst_pk
        primary key (req_dt, req_seq, tgt_clc, prcs_seq)
);

alter table sf_at_mopm_rnk_agg_rst owner to postgres;

create table sf_at_mopm_rnk_rst
(
    req_dt varchar(8) not null,
    req_seq numeric(3) not null,
    tgt_clc varchar(2) not null,
    wk_clc varchar(2) not null,
    crt_sta_dt varchar(8) not null,
    crt_end_dt varchar(8) not null,
    sta_dtm varchar(14),
    end_dtm varchar(14),
    tgt_cnt numeric(8) not null,
    prcs_sts_clc varchar(1) not null,
    err_ctnt varchar(2000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_mopm_rnk_rst_pk
        primary key (req_dt, req_seq, tgt_clc)
);

alter table sf_at_mopm_rnk_rst owner to postgres;

create table sf_at_rule_rcmd
(
    rcmd_mgt_id varchar(8) not null
        constraint sf_at_rule_rcmd_pk
            primary key,
    rcmd_mgt_nm varchar(50),
    rcmd_exec_mth_clc varchar(1),
    rcmd_mgt_sts_clc varchar(1),
    ruleset_id varchar(8),
    rule_tp_clc varchar(1),
    dataset_id varchar(10) not null,
    scr_use_yn varchar(1),
    expln_ctnt varchar(100),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    tgt_dataset_id varchar(255)
);

alter table sf_at_rule_rcmd owner to postgres;

create table sf_at_rule_rcmd_cnd_mgt
(
    rcmd_mgt_id varchar(8) not null,
    reg_seq numeric(3) not null,
    var_id varchar(50),
    oprt_nm varchar(50),
    val_ctnt varchar(1000),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_at_rule_rcmd_cnd_mgt_pk
        primary key (rcmd_mgt_id, reg_seq)
);

alter table sf_at_rule_rcmd_cnd_mgt owner to postgres;

create table sf_at_rule_rcmd_detc_rst
(
    req_dt varchar(8) not null,
    rcmd_mgt_id varchar(8) not null,
    req_seq numeric(3) not null,
    cnd_seq numeric(3) not null,
    cnd_ctnt varchar(1000),
    detc_cnt numeric(8),
    acum_detc_cnt numeric(8),
    hit_cnt numeric(8),
    acum_hit_cnt numeric(8),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_at_rule_rcmd_detc_rst_pk
        primary key (req_dt, rcmd_mgt_id, req_seq, cnd_seq)
);

alter table sf_at_rule_rcmd_detc_rst owner to postgres;

create table sf_at_rule_rcmd_exec_mgt
(
    rcmd_mgt_id varchar(255) not null
        constraint sf_at_rule_rcmd_exec_mgt_pkey
            primary key,
    chg_stm timestamp,
    chg_usr_id varchar(255),
    crt_prd_val integer,
    crt_sta_val integer,
    detc_tgt_prd_val integer,
    detc_tgt_sta_val integer,
    exec_crt_val integer,
    reg_stm timestamp,
    reg_usr_id varchar(255)
);

alter table sf_at_rule_rcmd_exec_mgt owner to postgres;

create table sf_at_rule_rcmd_prcs_hst
(
    req_dt varchar(8) not null,
    rcmd_mgt_id varchar(8) not null,
    req_seq numeric(3) not null,
    plan_sta_dtm varchar(14),
    sta_dtm varchar(14),
    end_dtm varchar(14),
    rcmd_prcs_sts_clc varchar(1),
    crt_sta_dt varchar(8),
    crt_end_dt varchar(8),
    crt_cnt numeric(8),
    crt_cnd_cnt numeric(8),
    detc_tgt_sta_dt varchar(8),
    detc_tgt_end_dt varchar(8),
    detc_tgt_cnt numeric(8),
    detc_tgt_cnd_cnt numeric(8),
    err_occr_dtm varchar(14),
    err_rsn_cd varchar(4),
    err_msg_ctnt varchar(200),
    memo_ctnt varchar(200),
    detc_cnt numeric(8),
    detc_agg_val_1 numeric(20),
    detc_agg_val_2 numeric(20),
    detc_agg_val_3 numeric(20),
    detc_agg_val_4 numeric(20),
    detc_agg_val_5 numeric(20),
    hit_cnt numeric(8),
    hit_agg_val_1 numeric(20),
    hit_agg_val_2 numeric(20),
    hit_agg_val_3 numeric(20),
    hit_agg_val_4 numeric(20),
    hit_agg_val_5 numeric(20),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_at_rule_rcmd_prcs_hst_pk
        primary key (req_dt, rcmd_mgt_id, req_seq)
);

alter table sf_at_rule_rcmd_prcs_hst owner to postgres;

create table sf_at_rule_siml
(
    siml_id varchar(8) not null
        constraint sf_at_rule_siml_pk
            primary key,
    ruleset_id varchar(8),
    rule_tp_clc varchar(1),
    siml_nm varchar(50),
    expln_ctnt varchar(100),
    siml_req_clc varchar(2),
    siml_req_id_ctnt varchar(100),
    rule_ctnt varchar(4000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_at_rule_siml owner to postgres;

create table sf_at_rule_siml_data
(
    crt_dt varchar(8) not null,
    data_clc varchar(2) not null,
    data_id_ctnt varchar(100) not null,
    data_ctnt text,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_rule_siml_data_pk
        primary key (crt_dt, data_clc, data_id_ctnt)
);

alter table sf_at_rule_siml_data owner to postgres;

create table sf_at_rule_siml_detc_rst
(
    req_dt varchar(8) not null,
    siml_id varchar(8) not null,
    req_seq numeric(3) not null,
    detc_tgt_id varchar(5) not null,
    detc_cnt numeric(8),
    detc_agg_val_1 numeric(20),
    detc_agg_val_2 numeric(20),
    detc_agg_val_3 numeric(20),
    detc_agg_val_4 numeric(20),
    detc_agg_val_5 numeric(20),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_rule_siml_detc_rst_pk
        primary key (req_dt, siml_id, req_seq, detc_tgt_id)
);

alter table sf_at_rule_siml_detc_rst owner to postgres;

create table sf_at_rule_siml_prcs_hst
(
    req_dt varchar(8) not null,
    siml_id varchar(8) not null,
    req_seq numeric(3) not null,
    plan_sta_dtm varchar(14),
    sta_dtm varchar(14),
    end_dtm varchar(14),
    siml_sts_clc varchar(1),
    crt_sta_dt varchar(8),
    crt_end_dt varchar(8),
    crt_cnt numeric(8),
    detc_tgt_sta_dt varchar(8),
    detc_tgt_end_dt varchar(8),
    detc_tgt_cnt numeric(8),
    err_occr_dtm varchar(14),
    err_rsn_cd varchar(4),
    err_msg_ctnt varchar(200),
    memo_ctnt varchar(200),
    rule_ctnt varchar(4000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_at_rule_siml_prcs_hst_pk
        primary key (req_dt, siml_id, req_seq)
);

alter table sf_at_rule_siml_prcs_hst owner to postgres;

create table sf_bt_btc_bs
(
    btc_id varchar(10) not null
        constraint sf_bt_btc_bs_pk
            primary key,
    btc_nm varchar(50) not null,
    upr_btc_id varchar(10),
    exec_schd_clc varchar(1) not null,
    exec_schd_ctnt varchar(50),
    dpdc_yn varchar(1) not null,
    dpdc_lvl varchar(3),
    expln_ctnt varchar(200),
    use_yn varchar(1) not null,
    btc_pgm_id varchar(50) not null,
    dfer_sta_dtm varchar(14),
    dfer_end_dtm varchar(14),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_bt_btc_bs owner to postgres;

create table sf_bt_btc_dpdc_mgt
(
    btc_id varchar(10) not null,
    dpdc_btc_id varchar(10) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_bt_btc_dpdc_mgt_pk
        primary key (btc_id, dpdc_btc_id)
);

alter table sf_bt_btc_dpdc_mgt owner to postgres;

create table sf_bt_btc_param_mgt
(
    btc_id varchar(10) not null,
    param_seq numeric(3) not null,
    param_id varchar(20) not null,
    necs_yn varchar(1) not null,
    expln_ctnt varchar(200),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_bt_btc_param_mgt_pk
        primary key (btc_id, param_seq)
);

alter table sf_bt_btc_param_mgt owner to postgres;

create table sf_bt_btc_prcs_hst
(
    crt_dt varchar(8) not null,
    btc_id varchar(10) not null,
    exec_seq numeric(3) not null,
    exec_tp_clc varchar(1) not null,
    prcs_sts_clc varchar(1) not null,
    plan_sta_dtm varchar(14) not null,
    sta_dtm varchar(14),
    end_dtm varchar(14),
    prcs_rst_ctnt varchar(4000),
    err_rsn_cd varchar(4),
    err_msg_ctnt varchar(4000),
    memo_ctnt varchar(200),
    param_ctnt varchar(4000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_bt_btc_prcs_hst_pk
        primary key (crt_dt, btc_id, exec_seq)
);

alter table sf_bt_btc_prcs_hst owner to postgres;

create table sf_cm_api
(
    api_id varchar(20) not null
        constraint sf_cm_api_pk
            primary key,
    api_nm varchar(100),
    api_tp_clc varchar(1) not null,
    api_exec_clc varchar(1) not null,
    api_addr_ctnt varchar(255) not null,
    menu_id varchar(20) not null,
    use_yn varchar(1) not null,
    hst_reg_yn varchar(1) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_api owner to postgres;

create table sf_cm_api_exec_hst
(
    exec_id varchar(20) not null
        constraint sf_cm_api_exec_hst_pk
            primary key,
    api_id varchar(20) not null,
    usr_id varchar(20) not null,
    exec_stm date not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_api_exec_hst owner to postgres;

create table sf_cm_api_role_mapp
(
    api_id varchar(50) not null,
    role_id varchar(20) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_cm_api_role_mapp_pk
        primary key (api_id, role_id)
);

alter table sf_cm_api_role_mapp owner to postgres;

create table sf_cm_cmd
(
    cmd_id varchar(50) not null
        constraint sf_cm_exec_pk
            primary key,
    cmd_nm varchar(50) not null,
    menu_id varchar(20) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_cmd owner to postgres;

create table sf_cm_cmd_role_mapp
(
    cmd_id varchar(50) not null,
    role_id varchar(20) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_cm_exec_role_mapp_pk
        primary key (cmd_id, role_id)
);

alter table sf_cm_cmd_role_mapp owner to postgres;

create table sf_cm_com_cd
(
    com_cd_tp_id varchar(6) not null,
    com_cd varchar(8) not null,
    com_cd_nm varchar(100) not null,
    com_cd_abrv_nm varchar(30),
    expln_ctnt varchar(500),
    use_yn varchar(1) not null,
    scrn_prnt_seq numeric(3),
    opt_ctnt varchar(50),
    cd_chg_yn varchar(1),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_cm_com_cd_pk
        primary key (com_cd_tp_id, com_cd)
);

alter table sf_cm_com_cd owner to postgres;

create table sf_cm_com_cd_tp
(
    com_cd_tp_id varchar(6) not null
        constraint sf_cm_com_cd_tp_pk
            primary key,
    com_cd_tp_nm varchar(100) not null,
    expln_ctnt varchar(100),
    cd_len numeric(3),
    cd_len_fix_yn varchar(1),
    cd_tp_chg_yn varchar(1) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_com_cd_tp owner to postgres;

create table sf_cm_db_chg_hst
(
    tbl_id varchar(20) not null,
    crt_dtm varchar(14) not null,
    col_id varchar(20) not null,
    bf_data_ctnt varchar(1000),
    chg_data_ctnt varchar(1000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_db_chg_hst owner to postgres;

create table sf_cm_dsbd_tgt_mgt
(
    svr_id varchar(50) not null,
    dsbd_ctg_clc varchar(2) not null,
    dsbd_tp_clc varchar(2) not null,
    scrn_prnt_seq numeric(3),
    req_port varchar(5),
    req_uri varchar(200),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_cm_dsbd_tgt_mgt_pk
        primary key (svr_id, dsbd_ctg_clc, dsbd_tp_clc)
);

alter table sf_cm_dsbd_tgt_mgt owner to postgres;

create table sf_cm_dsbd_tgt_prcs_hist
(
    svr_id varchar(50) not null,
    dsbd_ctg_clc varchar(2) not null,
    dsbd_tp_clc varchar(2) not null,
    tgt_tp_clc varchar(2) not null,
    cri_dt varchar(8) not null,
    cri_tm varchar(6) not null,
    pid varchar(100),
    val varchar(2000),
    opt_ctnt varchar(200),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_cm_dsbd_tgt_prcs_hist_pk
        primary key (svr_id, dsbd_ctg_clc, dsbd_tp_clc, tgt_tp_clc, cri_dt, cri_tm)
);

alter table sf_cm_dsbd_tgt_prcs_hist owner to postgres;

create table sf_cm_dt
(
    dt varchar(8) not null
        constraint sf_cm_dt_pk
            primary key,
    dt_clc varchar(1) not null,
    hldy_clc varchar(1),
    expln_ctnt varchar(100),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_dt owner to postgres;

create table sf_cm_menu
(
    menu_id varchar(20) not null
        constraint sf_cm_menu_pk
            primary key,
    menu_prnt_seq numeric(3) not null,
    menu_path varchar(20),
    menu_nm varchar(20) not null,
    upr_menu_id varchar(20),
    menu_icon_nm varchar(50),
    menu_color_nm varchar(50),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_menu owner to postgres;

create table sf_cm_menu_role_mapp
(
    menu_id varchar(20) not null,
    role_id varchar(20) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_cm_menu_role_mapp_pk
        primary key (menu_id, role_id)
);

alter table sf_cm_menu_role_mapp owner to postgres;

create table sf_cm_role
(
    role_id varchar(20) not null
        constraint sf_cm_role_pk
            primary key,
    role_nm varchar(100) not null,
    expln_ctnt varchar(100),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_role owner to postgres;

create table sf_cm_svr_mgt
(
    svr_id varchar(50) not null
        constraint sf_cm_svr_mgt_pk
            primary key,
    svr_tp_clc varchar(2),
    svr_nm varchar(50),
    svr_ip varchar(15),
    scrn_prnt_seq numeric(3),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date
);

alter table sf_cm_svr_mgt owner to postgres;

create table sf_cm_url_inq_hst
(
    crt_dtm varchar(255) not null,
    url_ctnt varchar(255) not null,
    usr_id varchar(255) not null,
    chg_stm timestamp,
    chg_usr_id varchar(255),
    menu_id varchar(255),
    reg_stm timestamp,
    reg_usr_id varchar(255),
    constraint sf_cm_url_inq_hst_pkey
        primary key (crt_dtm, url_ctnt, usr_id)
);

alter table sf_cm_url_inq_hst owner to postgres;

create table sf_cm_url_mgt
(
    url_ctnt varchar(50) not null
        constraint sf_cm_url_pk
            primary key,
    url_nm varchar(30),
    menu_id varchar(20),
    repo_lst varchar(500),
    inq_hst_reg_yn varchar(1),
    chg_hst_reg_yn varchar(1),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date
);

alter table sf_cm_url_mgt owner to postgres;

create table sf_cm_usr
(
    usr_id varchar(20) not null
        constraint sf_cm_usr_pk
            primary key,
    usr_nm varchar(50) not null,
    usr_sts_clc varchar(1) not null,
    pwd_ctnt varchar(500) not null,
    sec_key varchar(200) not null,
    pwd_err_nts numeric(3),
    pwd_chg_necs_yn varchar(1),
    pwd_lt_chg_dtm varchar(14),
    login_ip_verf_yn varchar(1) not null,
    login_ip varchar(15),
    dptm_id varchar(10),
    dptm_nm varchar(50),
    mbphn_pno varchar(15),
    offc_pno varchar(15),
    email varchar(50),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_cm_usr owner to postgres;

create table sf_cm_usr_conn_hst
(
    usr_id varchar(20) not null,
    crt_dtm varchar(14) not null,
    conn_sts_clc varchar(1) not null,
    login_ip varchar(15),
    clr_rsn_clc varchar(1),
    clr_usr_id varchar(20),
    clr_dtm varchar(14),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_cm_usr_conn_hst_pk
        primary key (usr_id, crt_dtm)
);

alter table sf_cm_usr_conn_hst owner to postgres;

create table sf_cm_usr_role_mapp
(
    usr_id varchar(20) not null,
    role_id varchar(20) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_cm_usr_role_mapp_pk
        primary key (usr_id, role_id)
);

alter table sf_cm_usr_role_mapp owner to postgres;

create table sf_lt_msg_layout_mgt
(
    msg_tp_id varchar(10) not null,
    item_seq numeric(3) not null,
    item_id varchar(50) not null,
    item_nm varchar(50) not null,
    item_len numeric(3) not null,
    item_tp_clc varchar(1) not null,
    expln_ctnt varchar(500),
    item_crat_clc varchar(1),
    item_crat_ref_ctnt varchar(500),
    key_yn varchar(1),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_lt_msg_layout_mgt_pk
        primary key (msg_tp_id, item_seq)
);

alter table sf_lt_msg_layout_mgt owner to postgres;

create table sf_lt_msg_mgt
(
    msg_tp_id varchar(10) not null
        constraint sf_lt_msg_mgt_pk
            primary key,
    msg_tp_nm varchar(50),
    layout_clc varchar(1) not null,
    layout_ref_ctnt varchar(10),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_lt_msg_mgt owner to postgres;

create table sf_lt_msg_prcs_log
(
    msg_guid varchar(50) not null
        constraint sf_lt_msg_prcs_log_pk
            primary key,
    crt_dt varchar(8) not null,
    msg_tp_id varchar(10) not null,
    msg_sbj_id varchar(50) not null,
    msg_ctnt varchar(4000) not null,
    prcs_sts_clc varchar(1) not null,
    prcs_dtm_1 varchar(20),
    prcs_dtm_2 varchar(20),
    prcs_dtm_3 varchar(20),
    prcs_dtm_4 varchar(20),
    prcs_dtm_5 varchar(20),
    prcs_svr_id varchar(10) not null,
    prcs_pgm_id varchar(10) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_lt_msg_prcs_log owner to postgres;

create index sf_lt_msg_prcs_log_idx_1
	on sf_lt_msg_prcs_log (crt_dt, msg_sbj_id);

create table sf_lt_msg_test_data
(
    msg_tp_id varchar(10) not null,
    reg_seq numeric(5) not null,
    test_data_id varchar(10),
    msg_ctnt varchar(4000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_lt_msg_test_data_pk
        primary key (msg_tp_id, reg_seq)
);

alter table sf_lt_msg_test_data owner to postgres;

create table sf_lt_msg_test_mgt
(
    test_id varchar(10) not null
        constraint sf_lt_msg_test_mgt_pk
            primary key,
    test_nm varchar(50),
    msg_tp_id varchar(10) not null,
    test_data_clc varchar(1) not null,
    test_data_ref_ctnt varchar(500),
    test_data_id varchar(10),
    trns_cnt numeric(8) not null,
    trns_rept_nts numeric(5) not null,
    trns_tps_val numeric(8,3) not null,
    trns_clc varchar(1) not null,
    trns_ref_ctnt varchar(1000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_lt_msg_test_mgt owner to postgres;

create table sf_lt_prcs_err_log
(
    msg_guid varchar(50) not null,
    reg_seq numeric(3) not null,
    crt_dt varchar(8) not null,
    err_tp_clc varchar(1) not null,
    err_tp_ctnt varchar(50),
    err_dtl_cd varchar(6),
    err_dtl_ctnt varchar(2000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_lt_prcs_err_log_pk
        primary key (msg_guid, reg_seq)
);

alter table sf_lt_prcs_err_log owner to postgres;

create table sf_ml_sbrf
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    prcs_dtm varchar(14) not null,
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    fds_chnl_clc varchar(3) not null,
    svc_cd varchar(50) not null,
    prcs_amt numeric(15) default 0 not null,
    cd_atm_tx_yn varchar(1),
    tnfr_yn varchar(1),
    ln_yn varchar(1),
    time_zone numeric(1),
    age numeric(3) not null,
    frgr_yn varchar(1),
    cst_inf_chg_yn varchar(1),
    acct_opn_elps_nds numeric(15,5),
    mm1_nftf_acct_opn_yn varchar(1),
    itnt_bnkn_appc_yn varchar(1),
    acct_tp_cd varchar(255),
    enty_tp_cd varchar(2),
    acct_fr_tx_yn varchar(1),
    non_tx_acct_yn varchar(1),
    acct_ln_exec_amt numeric(15,5) default 0 not null,
    acct_ln_exec_rgaf_tx_elps_nds numeric(15,5),
    acct_mm6_non_biz_day_tx_cnt numeric(15,5),
    acct_mm6_biz_ot_tx_cnt numeric(15,5),
    acct_mm6_tnfr_cnt numeric(15,5),
    acct_mm6_ovs_rmtn_cnt numeric(15,5),
    acct_mm6_m_avg_tnfr_cnt numeric(15,5),
    acct_mm6_rcmy_tx_samt numeric(15,5) default 0 not null,
    acct_mm6_tnfr_rcmy_tx_samt numeric(15,5) default 0 not null,
    acct_mm6_rcmy_tx_cnt numeric(15,5),
    acct_mm6_tnfr_rcmy_tx_cnt numeric(15,5),
    acct_mm1_bf_tx_xn varchar(1),
    acct_mm6_bf_tnfr_d_afte_elps_nds numeric(15,5),
    acct_mm6_bf_rcmy_d_afte_elps_nds numeric(15,5),
    acct_mm6_bf_wtdw_d_afte_elps_nds numeric(15,5),
    acct_mm6_tot_tx_cnt numeric(15,5),
    acct_mm6_max_tnfr_rcmy_tx_amt numeric(15,5) default 0 not null,
    acct_mm6_max_tnfr_wtdw_tx_amt numeric(15,5) default 0 not null,
    acct_mm6_wtdw_tx_samt numeric(15,5) default 0 not null,
    acct_mm6_tnfr_wtdw_tx_samt numeric(15,5) default 0 not null,
    acct_mm6_wtdw_tx_cnt numeric(15,5),
    acct_mm6_tnfr_wtdw_tx_cnt numeric(15,5),
    acct_mm1_otbk_tnfr_yn varchar(1),
    acct_mm6_otbk_tnfr_yn varchar(1),
    acct_mm6_avg_tnfr_tx_cnt numeric(15,5),
    acct_mm6_avg_rcmy_tx_amt numeric(15,5) default 0 not null,
    acct_mm6_avg_wtdw_tx_amt numeric(15,5) default 0 not null,
    acct_mm6a_mm1s_biz_ot_tx_cnt_rt numeric(15,5) default 0 not null,
    acct_mm6a_mm1s_biz_ot_wtdw_amt_rt numeric(15,5),
    acct_mm6a_mm1s_biz_ot_rcmy_amt_rt numeric(15,5),
    acct_mm6a_mm1s_tnfr_rcmy_amt_rt numeric(15,5),
    acct_mm6a_mm1s_tnfr_cnt_rt numeric(15,5),
    acct_mm6a_mm1s_ovs_rmtn_amt_rt numeric(15,5),
    acct_amtrng_use_prbl numeric(15,5),
    acct_time_zone_use_prbl numeric(15,5),
    acct_chnl_cl_use_prbl numeric(15,5),
    cst_mm6_cd_atm_tx_cnt numeric(15,5),
    cst_mm1_ln_exec_yn varchar(1),
    cst_mm1_tnfr_wtdw_tx_cnt numeric(15,5),
    cst_mm1_tnfr_wtdw_tx_samt numeric(15,5) default 0 not null,
    cst_mm1_max_tnfr_wtdw_tx_amt numeric(15,5) default 0 not null,
    cst_mm1_tnfr_rcmy_tx_cnt numeric(15,5),
    cst_mm1_tnfr_rcmy_tx_samt numeric(15,5) default 0 not null,
    cst_mm1_max_tnfr_rcmy_tx_amt numeric(15,5) default 0 not null,
    cst_mm1_pr_ip_conn_cnt numeric(15,5),
    cst_mm1_pr_hdd_conn_cnt numeric(15,5),
    cst_mm1_pr_mac_conn_cnt numeric(15,5),
    vctm_acct_scr numeric(15,5),
    fdbp_scr numeric(15,5),
    vctm_acct_rsn_scr_cd varchar(10),
    fdbp_rsn_cd varchar(10),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_ml_sbrf_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_ml_sbrf owner to postgres;

create table sf_mo_ctc_mgt
(
    ctc_tgt_id varchar(50) not null,
    ctc_dtm varchar(14) not null,
    ctc_tgt_tp_clc varchar(2),
    ctc_chnl_clc varchar(2),
    ctc_mth_clc varchar(1),
    ctc_rst_clc varchar(1),
    ctc_rel_clc varchar(2),
    ctc_ctnt varchar(255),
    tx_guid_no varchar(36),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_mo_ctc_mgt_pk
        primary key (ctc_tgt_id, ctc_dtm)
);

alter table sf_mo_ctc_mgt owner to postgres;

create table sf_mo_memo_mgt
(
    memo_tgt_id varchar(50) not null,
    memo_wrt_dtm varchar(14) not null,
    memo_tgt_tp_clc varchar(2),
    memo_tp_clc varchar(2),
    memo_ctnt varchar(255),
    tx_guid_no varchar(36),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_mo_memo_mgt_pk
        primary key (memo_tgt_id, memo_wrt_dtm)
);

alter table sf_mo_memo_mgt owner to postgres;

create table sf_ms_crd_bs
(
    crdid varchar(19) not null
        constraint sf_ms_crd_bs_pk
            primary key,
    iss_dt varchar(8),
    vld_ym varchar(6),
    crd_sts_cd varchar(4),
    cls_dt varchar(8),
    cls_rsn_cd varchar(4),
    bl_cd varchar(4),
    rpst_crdid varchar(19),
    crd_tp_clc varchar(1),
    owr_tp_clc varchar(1),
    owr_rel_clc varchar(1),
    owr_cstno varchar(10),
    isu_cstno varchar(10),
    mbrno varchar(10),
    brn_clc varchar(2),
    fr_iss_dt varchar(8),
    crd_grd_cd varchar(2),
    crd_gds_cd varchar(6),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date
);

alter table sf_ms_crd_bs owner to postgres;

create table sf_ms_cst_bs
(
    cstno varchar(10) not null
        constraint sf_ms_cst_bs_pk
            primary key,
    cst_tp_clc varchar(1),
    cst_brth_dt varchar(8),
    cst_nm varchar(50),
    gnd_clc varchar(1),
    cst_grd_cd varchar(2),
    cst_sts_cd varchar(4),
    cls_dt varchar(8),
    cls_rsn_cd varchar(4),
    bl_cd varchar(4),
    home_zc varchar(5),
    home_addr_ctnt varchar(100),
    offc_nm varchar(50),
    offc_dptm_nm varchar(50),
    offc_pstn_nm varchar(20),
    offc_zc varchar(5),
    offc_addr_ctnt varchar(100),
    home_pno varchar(15),
    offc_pno varchar(15),
    mbphn_pno varchar(15),
    email varchar(50),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date
);

comment on column sf_ms_cst_bs.cst_brth_dt is '생년월일';

alter table sf_ms_cst_bs owner to postgres;

create table sf_ms_fds_acnt
(
    acnt_id varchar(50) not null
        constraint sf_ms_fds_acnt_pk
            primary key,
    hts_id varchar(50),
    clnt_enty_id varchar(50) not null,
    acnt_type_code varchar(10),
    acnt_no_cryp varchar(255),
    acnt_name varchar(100),
    acnt_stas_type_code varchar(2),
    opng_date varchar(8),
    clsg_date varchar(8),
    mngr_empy_no varchar(50),
    mngg_brnh_enty_id varchar(50),
    onln_opng_type_code varchar(2),
    cnst_acnt_no_cryp varchar(255),
    pvdd_blnc_amnt numeric(15) default 0 not null,
    ttdd_blnc_amnt numeric(15) default 0 not null,
    non_face_self_cnfm_mthd_code varchar(1),
    non_face_self_cfdt varchar(8),
    non_face_mobl_phon_no_cryp varchar(255),
    non_face_tlcm_sect_code varchar(1),
    non_face_wtdw_bank_code varchar(3),
    non_face_afw_no_cryp varchar(255),
    fnc_trdg_prps_cnfm_code varchar(1),
    non_face_prce_chnl_sect_code varchar(3),
    stck_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    wthd_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    bond_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    rp_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    fnpr_etc_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    mmf_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    fund_mtl_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    ints_etc_asst_vltn_blnc_amnt numeric(15) default 0 not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    mngg_brnh_enty_enty_id varchar(255),
    non_face_athn_mobl_phon_no_cryp varchar(255)
);

alter table sf_ms_fds_acnt owner to postgres;

create table sf_ms_fds_clnt
(
    clnt_enty_id varchar(50) not null
        constraint sf_ms_fds_clnt_pk
            primary key,
    clnt_name varchar(300),
    enty_type_code varchar(2),
    rrno_cryp varchar(64),
    bzno_cryp varchar(64),
    cpno_cryp varchar(64),
    ntny_id numeric(10),
    rsdn_rgsn_brdt varchar(8),
    gndr_type_code varchar(1),
    hts_id varchar(20),
    hts_itni_ctrt_yn varchar(1),
    vip_yn varchar(1),
    mngn_yn varchar(1),
    mngr_empy_no varchar(16),
    mngn_dtbr_code varchar(5),
    phon_enct_type_code varchar(2),
    cnty_phon_code_cryp varchar(64),
    area_code_cryp varchar(64),
    phon_ecno_cryp varchar(32),
    phon_no_cryp varchar(64),
    cnty_mobl_phon_code_cryp varchar(64),
    mobl_phon_idnf_no_cryp varchar(32),
    mobl_phon_ecno_cryp varchar(32),
    mobl_phon_no_cryp varchar(64),
    emal_id_cryp varchar(64),
    emal_host_name_cryp varchar(320),
    adrs_enct_type_code varchar(2),
    pstl_code varchar(6),
    bsc_adrs_cryp varchar(320),
    dtls_adrs_cryp varchar(320),
    city_id numeric(10),
    stat_name varchar(90),
    new_adrs_yn varchar(1),
    road_name_bldn_no varchar(30),
    lttd_crdn numeric(15,6),
    lngd_crdn numeric(15,6),
    bnkn_pc_rgsn_stas_code varchar(1),
    otp_isnc_date_time varchar(14),
    pblc_crtc_isnc_date varchar(8),
    prva_crtc_isnc_date varchar(8),
    opbk_user_rgsn_stas_code varchar(1),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    area_phon_code_cryp varchar(255),
    hts_itnt_ctrt_yn varchar(255),
    prva_crtc_isnc_dtae varchar(255)
);

alter table sf_ms_fds_clnt owner to postgres;

create table sf_ms_jnt_mct_bs
(
    crno varchar(10) not null
        constraint sf_ms_jnt_mct_bs_pk
            primary key,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    std_mcc varchar(4),
    mct_nm varchar(50),
    mct_zc varchar(5),
    mct_addr_ctnt varchar(100),
    mct_pno varchar(15)
);

alter table sf_ms_jnt_mct_bs owner to postgres;

create table sf_ms_mbr_bs
(
    mbrno varchar(10) not null
        constraint sf_ms_mbr_bs_pk
            primary key,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    mbr_tp_clc varchar(1),
    cstno varchar(10),
    stt_d varchar(2),
    mbr_sts_cd varchar(4),
    cls_dt varchar(8),
    cls_rsn_cd varchar(4),
    bl_cd varchar(4),
    tot_lmt_amt numeric(18),
    ca_lmt_amt numeric(18),
    ovs_lmt_amt numeric(18)
);

alter table sf_ms_mbr_bs owner to postgres;

create table sf_ms_mct_bs
(
    mctno varchar(20) not null
        constraint sf_ms_mct_bs_pk
            primary key,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    crno varchar(10),
    cnrt_mcc varchar(4),
    std_mcc varchar(4),
    cnrt_dt varchar(8),
    opn_dt varchar(8),
    mct_sts_cd varchar(4),
    cls_dt varchar(8),
    cls_rsn_cd varchar(4),
    bl_cd varchar(4),
    mct_nm varchar(50),
    mct_zc varchar(5),
    mct_addr_ctnt varchar(100),
    mct_pno varchar(15),
    dgt_cstno varchar(10)
);

alter table sf_ms_mct_bs owner to postgres;

create table sf_mt_cnc
(
    cnc varchar(3) not null
        constraint sf_mt_cnc_pk
            primary key,
    eng_cnc_1 varchar(3),
    eng_cnc_2 varchar(2),
    curc_cd varchar(3),
    cnc_nm_1 varchar(50),
    cnc_nm_2 varchar(50),
    gmt_tmdff_n numeric(2),
    loc_inf_ctnt varchar(100),
    opt_ctnt varchar(100),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date
);

alter table sf_mt_cnc owner to postgres;

create table sf_mt_dom_area
(
    area_clc varchar(1) not null,
    area_id varchar(6) not null,
    area_nm_1 varchar(50),
    area_nm_2 varchar(50),
    loc_inf_ctnt varchar(100),
    opt_ctnt varchar(100),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_mt_dom_area_pk
        primary key (area_clc, area_id)
);

alter table sf_mt_dom_area owner to postgres;

create table sf_mt_mcc
(
    mcc_clc varchar(1) not null,
    mcc varchar(4) not null,
    rpst_mcc varchar(4),
    mcc_nm_1 varchar(50),
    mcc_nm_2 varchar(50),
    opt_ctnt varchar(100),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_mt_mcc_pk
        primary key (mcc_clc, mcc)
);

alter table sf_mt_mcc owner to postgres;

create table sf_mt_ovs_area
(
    area_clc varchar(1) not null,
    area_id varchar(20) not null,
    area_nm_1 varchar(50),
    area_nm_2 varchar(50),
    gmt_tmdff_n numeric(2),
    loc_inf_ctnt varchar(100),
    opt_ctnt varchar(100),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_mt_ovs_area_pk
        primary key (area_clc, area_id)
);

alter table sf_mt_ovs_area owner to postgres;

create table sf_pf_acnt
(
    acct_id varchar(50) not null
        constraint sf_pf_acnt_pk
            primary key,
    bf_rcmy_dtm varchar(14),
    bf_rcmy_amt numeric(15) default 0,
    bf_wtdw_dtm varchar(14),
    bf_wtdw_amt numeric(15) default 0,
    bf_tnfr_rcmy_dtm varchar(14) not null,
    bf_tnfr_rcmy_amt numeric(15) default 0 not null,
    bf_tnfr_wtdw_dtm varchar(14),
    bf_tnfr_wtdw_amt numeric(15) default 0,
    bf_ovs_bnkn_tx_dtm varchar(14),
    bf_ln_appc_dtm varchar(14),
    bf_bamt_rfrn_dtm varchar(14),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_pf_acnt owner to postgres;

create table sf_pf_clnt
(
    cst_id varchar(50) not null
        constraint sf_pf_clnt_pk
            primary key,
    bf_ln_appc_dtm varchar(14),
    bf_bnkn_tx_dtm varchar(14) not null,
    ln_exec_tot_cnt numeric(9) default 0,
    bf_mobl_phon_no_cryp varchar(255),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_pf_clnt owner to postgres;

create table sf_pf_conn_trm
(
    trm_gthr_clc varchar(2) not null,
    trm_gthr_ntv_id varchar(255) not null,
    cst_id varchar(50) not null,
    fr_conn_dtm varchar(14) not null,
    bf_conn_dtm varchar(14) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_pf_conn_trm_pk
        primary key (trm_gthr_clc, trm_gthr_ntv_id, cst_id)
);

alter table sf_pf_conn_trm owner to postgres;

create table sf_pf_crd_dom_prf
(
    crdid varchar(19) not null,
    crt_ym varchar(6) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    mst_crt_dtm varchar(14),
    iss_dt varchar(8),
    crd_tp_clc varchar(1),
    owr_tp_clc varchar(1),
    owr_rel_clc varchar(1),
    owr_cstno varchar(10),
    isu_cstno varchar(10),
    mbrno varchar(10),
    cst_tp_clc varchar(1),
    cst_brth_dt varchar(8),
    gnd_clc varchar(1),
    mbr_tp_clc varchar(1),
    stt_d varchar(2),
    bf_tx_dtm varchar(14),
    bf_tx_cd varchar(3),
    ap_cnt numeric(5),
    ap_amt numeric(15),
    ap_nds numeric(3),
    lt_ap_dt varchar(8),
    m_max_ap_amt numeric(15),
    et_max_ap_amt numeric(15),
    amt_ctg_cnt_1 numeric(5),
    amt_ctg_cnt_2 numeric(5),
    amt_ctg_cnt_3 numeric(5),
    amt_ctg_cnt_4 numeric(5),
    amt_ctg_cnt_5 numeric(5),
    dt_ctg_cnt_1 numeric(5),
    dt_ctg_cnt_2 numeric(5),
    dt_ctg_amt_1 numeric(15),
    dt_ctg_amt_2 numeric(15),
    mcc_cnt_1 numeric(5),
    mcc_amt_1 numeric(15),
    mcc_max_amt_1 numeric(15),
    mcc_nds_1 numeric(3),
    mcc_lt_dt_1 varchar(8),
    mcc_cnt_2 numeric(5),
    mcc_amt_2 numeric(15),
    mcc_max_amt_2 numeric(15),
    mcc_nds_2 numeric(3),
    mcc_lt_dt_2 varchar(8),
    lt_au_dtm varchar(14),
    lt_au_ip varchar(15),
    constraint sf_pf_crd_dom_prf_pk
        primary key (crdid, crt_ym)
);

alter table sf_pf_crd_dom_prf owner to postgres;

create table sf_pf_crd_fnc_prf
(
    crdid varchar(19) not null,
    crt_ym varchar(6) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    bf_tx_dtm varchar(14),
    constraint sf_pf_crd_fnc_prf_pk
        primary key (crdid, crt_ym)
);

alter table sf_pf_crd_fnc_prf owner to postgres;

create table sf_pf_crd_ovs_ftf_prf
(
    crdid varchar(19) not null,
    crt_y varchar(4) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    bf_tx_dtm varchar(14),
    constraint sf_pf_crd_ovs_ftf_prf_pk
        primary key (crdid, crt_y)
);

alter table sf_pf_crd_ovs_ftf_prf owner to postgres;

create table sf_pf_crd_ovs_nftf_prf
(
    crdid varchar(19) not null,
    crt_y varchar(4) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    bf_tx_dtm varchar(14),
    constraint sf_pf_crd_ovs_nftf_prf_pk
        primary key (crdid, crt_y)
);

alter table sf_pf_crd_ovs_nftf_prf owner to postgres;

create table sf_pf_cst_prf
(
    cstno varchar(10) not null
        constraint sf_pf_cst_prf_pk
            primary key,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    bf_tx_dtm varchar(14)
);

alter table sf_pf_cst_prf owner to postgres;

create table sf_pf_mct_dom_prf
(
    mct_clc varchar(1) not null,
    mct_id varchar(20) not null,
    mct_sub_id varchar(20) not null,
    crt_ym varchar(6) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    mst_crt_dtm varchar(14),
    crno varchar(10),
    mct_zc varchar(5),
    bf_tx_dtm varchar(14),
    ap_cnt numeric(8),
    ap_amt numeric(15),
    ap_nds numeric(3),
    lt_ap_dt varchar(8),
    m_max_ap_amt numeric(15),
    et_max_ap_amt numeric(15),
    amt_ctg_cnt_1 numeric(5),
    amt_ctg_cnt_2 numeric(5),
    amt_ctg_cnt_3 numeric(5),
    amt_ctg_cnt_4 numeric(5),
    amt_ctg_cnt_5 numeric(5),
    dt_ctg_cnt_1 numeric(8),
    dt_ctg_cnt_2 numeric(8),
    dt_ctg_amt_1 numeric(15),
    dt_ctg_amt_2 numeric(15),
    age_ctg_cnt_1 numeric(8),
    age_ctg_cnt_2 numeric(8),
    age_ctg_cnt_3 numeric(8),
    age_ctg_cnt_4 numeric(8),
    age_ctg_amt_1 numeric(15),
    age_ctg_amt_2 numeric(15),
    age_ctg_amt_3 numeric(15),
    age_ctg_amt_4 numeric(15),
    constraint sf_pf_mct_dom_prf_pk
        primary key (mct_clc, mct_id, mct_sub_id, crt_ym)
);

alter table sf_pf_mct_dom_prf owner to postgres;

create table sf_pf_mct_ovs_prf
(
    mct_id varchar(20) not null,
    crt_y varchar(4) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    bf_tx_dtm varchar(14),
    constraint sf_pf_mct_ovs_prf_pk
        primary key (mct_id, crt_y)
);

alter table sf_pf_mct_ovs_prf owner to postgres;

create table sf_pf_tx_prf
(
    crdid varchar(19) not null,
    tx_dtm varchar(14) not null,
    tx_ntv_no varchar(12) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_pf_tx_prf_pk
        primary key (crdid, tx_dtm, tx_ntv_no)
);

alter table sf_pf_tx_prf owner to postgres;

create table sf_rp_rule_pfmc
(
    crt_dt varchar(8) not null,
    rule_id varchar(8) not null,
    detc_tgt_id varchar(5) not null,
    detc_cnt numeric(8),
    detc_agg_val_1 numeric(20),
    detc_agg_val_2 numeric(20),
    detc_agg_val_3 numeric(20),
    detc_agg_val_4 numeric(20),
    detc_agg_val_5 numeric(20),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_rp_rule_pfmc_pk
        primary key (crt_dt, rule_id, detc_tgt_id)
);

alter table sf_rp_rule_pfmc owner to postgres;

create table sf_st_blck_mgt
(
    blck_tgt_tp_clc varchar(2) not null,
    blck_tgt_id varchar(50) not null,
    reg_seq numeric(5) not null,
    blck_clc varchar(1) not null,
    blck_acct_tp_clc varchar(4) not null,
    apprv_sts_clc varchar(2),
    reg_rsn_ctnt varchar(200),
    clr_rsn_ctnt varchar(200),
    clr_dtm varchar(14),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_blck_mgt_pk
        primary key (blck_tgt_tp_clc, blck_tgt_id, reg_seq, blck_clc, blck_acct_tp_clc)
);

alter table sf_st_blck_mgt owner to postgres;

create table sf_st_blck_opr_mgt
(
    req_dt varchar(8) not null,
    req_seq numeric(3) not null,
    blck_opr_tgt_clc varchar(1),
    apprv_sts_clc varchar(2),
    blck_tgt_tp_clc varchar(2),
    blck_tgt_id varchar(50),
    reg_seq numeric(5),
    req_usr_id varchar(20),
    req_msg_ctnt varchar(200),
    siml_appl_rslt_ctnt varchar(200),
    ap_usr_id varchar(20),
    ap_msg_ctnt varchar(200),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_blck_opr_mgt_pk
        primary key (req_dt, req_seq)
);

alter table sf_st_blck_opr_mgt owner to postgres;

create table sf_st_frd_mgt
(
    frd_src_clc varchar(2) not null,
    frd_tgt_tp_clc varchar(2) not null,
    frd_tgt_id varchar(100) not null,
    reg_seq numeric(5) not null,
    frd_risk_grdc varchar(1),
    frd_risk_pont numeric(2),
    acct_tp_larg_clc varchar(1),
    acct_tp_sml_clc varchar(4),
    acct_sbj_tp_clc varchar(1),
    apprv_sts_clc varchar(2),
    reg_rsn_ctnt varchar(200),
    clr_rsn_ctnt varchar(200),
    clr_dtm varchar(14),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_frd_mgt_pk
        primary key (frd_src_clc, frd_tgt_tp_clc, frd_tgt_id, reg_seq)
);

alter table sf_st_frd_mgt owner to postgres;

create table sf_st_plc_ex_mgt
(
    plc_biz_clc varchar(1) not null,
    plc_tgt_tp_clc varchar(2) not null,
    plc_tgt_id varchar(100) not null,
    reg_seq numeric(5) not null,
    plc_appl_tp_clc varchar(2),
    appl_sta_dtm varchar(14),
    appl_end_dtm varchar(14),
    reg_rsn_ctnt varchar(200),
    clr_rsn_ctnt varchar(200),
    clr_dtm varchar(14),
    plc_reg_rsn_clc varchar(2),
    plc_cnd_tp_clc_1 varchar(2),
    plc_cnd_tp_ctnt_1 varchar(100),
    plc_cnd_tp_clc_2 varchar(2),
    plc_cnd_tp_ctnt_2 varchar(100),
    plc_cnd_tp_clc_3 varchar(2),
    plc_cnd_tp_ctnt_3 varchar(100),
    plc_cnd_tp_clc_4 varchar(2),
    plc_cnd_tp_ctnt_4 varchar(100),
    plc_cnd_tp_clc_5 varchar(2),
    plc_cnd_tp_ctnt_5 varchar(100),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_plc_ex_mgt_pk
        primary key (plc_biz_clc, plc_tgt_tp_clc, plc_tgt_id, reg_seq)
);

alter table sf_st_plc_ex_mgt owner to postgres;

create table sf_st_plc_mgt
(
    plc_biz_clc varchar(1) not null,
    plc_tgt_tp_clc varchar(2) not null,
    plc_tgt_id varchar(100) not null,
    reg_seq numeric(5) not null,
    plc_appl_tp_clc varchar(2),
    appl_sta_dtm varchar(14),
    appl_end_dtm varchar(14),
    reg_rsn_ctnt varchar(200),
    clr_rsn_ctnt varchar(200),
    clr_dtm varchar(14),
    plc_cnd_tp_clc_1 varchar(2),
    plc_cnd_tp_ctnt_1 varchar(100),
    plc_cnd_tp_clc_2 varchar(2),
    plc_cnd_tp_ctnt_2 varchar(100),
    plc_cnd_tp_clc_3 varchar(2),
    plc_cnd_tp_ctnt_3 varchar(100),
    plc_cnd_tp_clc_4 varchar(2),
    plc_cnd_tp_ctnt_4 varchar(100),
    plc_cnd_tp_clc_5 varchar(2),
    plc_cnd_tp_ctnt_5 varchar(100),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_plc_mgt_pk
        primary key (plc_biz_clc, plc_tgt_tp_clc, plc_tgt_id, reg_seq)
);

alter table sf_st_plc_mgt owner to postgres;

create table sf_st_plc_var
(
    plc_biz_clc varchar(1) not null,
    plc_tgt_tp_clc varchar(2) not null,
    plc_tgt_id varchar(100) not null,
    plc_var_id varchar(7) not null,
    reg_seq numeric(5) not null,
    plc_prps_clc varchar(2),
    appl_sta_dtm varchar(14),
    appl_end_dtm varchar(14),
    reg_rsn_ctnt varchar(200),
    clr_rsn_ctnt varchar(200),
    clr_dtm varchar(14),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_plc_var_pk
        primary key (plc_biz_clc, plc_tgt_tp_clc, plc_tgt_id, plc_var_id, reg_seq)
);

alter table sf_st_plc_var owner to postgres;

create table sf_st_plc_var_mgt
(
    plc_biz_clc varchar(1) not null,
    plc_var_id varchar(7) not null,
    plc_var_nm varchar(30),
    plc_var_expln_ctnt varchar(300),
    plc_prps_clc varchar(2),
    del_yn varchar(1),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_plc_var_mgt_pk
        primary key (plc_biz_clc, plc_var_id)
);

alter table sf_st_plc_var_mgt owner to postgres;

create table sf_st_rule
(
    rule_id varchar(8) not null,
    rule_ver numeric(3) not null,
    rule_wk_seq numeric(3) not null,
    ruleset_id varchar(8) not null,
    rule_tp_clc varchar(1) not null,
    rule_act_clc varchar(2) not null,
    rule_sts_clc varchar(2) not null,
    rule_nm varchar(100) not null,
    cnd_ctnt varchar(4000),
    act_ctnt varchar(4000),
    expln_ctnt varchar(1000),
    appl_yn varchar(1) not null,
    appl_sta_dtm varchar(14) not null,
    appl_end_dtm varchar(14) not null,
    prcs_seq numeric(3) not null,
    prit_seq numeric(3) not null,
    siml_yn varchar(1),
    opt_ctnt varchar(50),
    verf_yn varchar(1) not null,
    verf_fail_rsn_ctnt varchar(200),
    appl_dtm varchar(14),
    clr_dtm varchar(14),
    prit_cnd_ctnt_1 varchar(1000),
    prit_cnd_ctnt_2 varchar(1000),
    prit_cnd_ctnt_3 varchar(1000),
    prit_cnd_ctnt_4 varchar(1000),
    prit_cnd_ctnt_5 varchar(1000),
    prit_cnd_ctnt_6 varchar(1000),
    act_memo_ctnt varchar(500),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_st_rule_pk
        primary key (rule_id, rule_ver)
);

comment on table sf_st_rule is '룰관리';

comment on column sf_st_rule.rule_id is '룰ID';

alter table sf_st_rule owner to postgres;

create table sf_st_rule_fnct
(
    fnct_nm varchar(50) not null
        constraint sf_st_rule_fnct_pk
            primary key,
    fnct_clc varchar(2) not null,
    fnct_stx_ctnt varchar(200),
    fnct_def_ctnt varchar(200),
    fnct_expln_ctnt varchar(1000),
    fnct_tmpl_ctnt varchar(200),
    scrn_prnt_seq numeric(3),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_st_rule_fnct owner to postgres;

create table sf_st_rule_hst
(
    rule_id varchar(8) not null,
    rule_ver numeric(3) not null,
    rule_wk_seq numeric(3) not null,
    ruleset_id varchar(8) not null,
    rule_tp_clc varchar(1) not null,
    rule_act_clc varchar(2) not null,
    rule_sts_clc varchar(2) not null,
    rule_nm varchar(50) not null,
    cnd_ctnt varchar(4000),
    act_ctnt varchar(4000),
    expln_ctnt varchar(100),
    appl_yn varchar(1) not null,
    appl_sta_dtm varchar(14) not null,
    appl_end_dtm varchar(14) not null,
    prcs_seq numeric(3) not null,
    prit_seq numeric(3) not null,
    siml_yn varchar(1),
    opt_ctnt varchar(50),
    verf_yn varchar(1) not null,
    verf_fail_rsn_ctnt varchar(200),
    appl_dtm varchar(14),
    clr_dtm varchar(14),
    prit_cnd_ctnt_1 varchar(1000),
    prit_cnd_ctnt_2 varchar(1000),
    prit_cnd_ctnt_3 varchar(1000),
    prit_cnd_ctnt_4 varchar(1000),
    prit_cnd_ctnt_5 varchar(1000),
    prit_cnd_ctnt_6 varchar(1000),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    act_memo_ctnt varchar(500),
    constraint sf_st_rule_hst_pk
        primary key (rule_id, rule_ver, rule_wk_seq)
);

alter table sf_st_rule_hst owner to postgres;

create table sf_st_rule_opr_mgt
(
    req_dt varchar(8) not null,
    req_seq numeric(3) not null,
    rule_opr_tgt_clc varchar(1) not null,
    rule_opr_sts_clc varchar(1) not null,
    rule_id varchar(8) not null,
    rule_ver numeric(3) not null,
    req_usr_id varchar(20) not null,
    req_msg_ctnt varchar(200),
    siml_appl_rslt_ctnt varchar(200),
    ap_usr_id varchar(20),
    ap_msg_ctnt varchar(200),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_rule_opr_mgt_pk
        primary key (req_dt, req_seq)
);

alter table sf_st_rule_opr_mgt owner to postgres;

create table sf_st_rule_prit_cnd_mgt
(
    rule_prit_cnd_id varchar(8) not null
        constraint sf_st_rule_prit_cnd_mgt_pk
            primary key,
    rule_prit_cnd_seq numeric(3) not null,
    var_id varchar(50) not null,
    oprt_lst varchar(50) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_st_rule_prit_cnd_mgt owner to postgres;

create table sf_st_rule_var
(
    var_id varchar(50) not null
        constraint sf_st_rule_var_pk
            primary key,
    var_nm varchar(50) not null,
    var_grp_id varchar(50) not null,
    use_yn varchar(1) not null,
    expln_ctnt varchar(100),
    var_tp_clc varchar(1) not null,
    var_use_clc varchar(1) not null,
    ref_clc varchar(1),
    ref_tgt_id varchar(20),
    scrn_prnt_seq numeric(3),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_st_rule_var owner to postgres;

create table sf_st_rule_var_grp
(
    var_grp_id varchar(50) not null
        constraint sf_st_rule_var_grp_pk
            primary key,
    var_grp_nm varchar(50) not null,
    use_yn varchar(1) not null,
    expln_ctnt varchar(100),
    scrn_prnt_seq numeric(3),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_st_rule_var_grp owner to postgres;

create table sf_st_ruleset
(
    ruleset_id varchar(8) not null
        constraint sf_st_ruleset_pk
            primary key,
    ruleset_nm varchar(50) not null,
    expln_ctnt varchar(100),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null
);

alter table sf_st_ruleset owner to postgres;

create table sf_st_ruleset_var
(
    ruleset_id varchar(8) not null,
    var_id varchar(50) not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_st_ruleset_var_pk
        primary key (ruleset_id, var_id)
);

alter table sf_st_ruleset_var owner to postgres;

create table sf_tx_ahnlab_mdti
(
    tx_base_ym varchar(255) not null,
    tx_guid_no varchar(255) not null,
    acct_id bigint,
    au_inf_gthr_dtm varchar(255),
    chg_stm timestamp,
    chg_usr_id varchar(255),
    chnl_clc varchar(255),
    cst_id bigint,
    fds_chnl_clc varchar(255),
    force_call varchar(255),
    hts_id varchar(255),
    prcs_rst_clc varchar(255),
    prcs_tp_clc varchar(255),
    reg_stm timestamp,
    reg_usr_id varchar(255),
    remote_control varchar(255),
    rooting varchar(255),
    special_authority varchar(255),
    svc_cd varchar(255),
    svc_id varchar(255),
    tx_scrn_no varchar(255),
    unidentified_installer varchar(255),
    constraint sf_tx_ahnlab_mdti_pkey
        primary key (tx_base_ym, tx_guid_no)
);

alter table sf_tx_ahnlab_mdti owner to postgres;

create table sf_tx_alt
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    alt_tx_rsn_clc varchar(2),
    rule_id varchar(8),
    rule_srch_ctnt varchar(255),
    rule_ex_ctnt varchar(255),
    rule_siml_ctnt varchar(255),
    dsp_bizwk_clc varchar(1),
    dsp_clc varchar(2),
    dsp_rule_id varchar(8),
    dsp_rule_srch_ctnt varchar(255),
    dsp_emp_no varchar(50),
    dsp_dtm varchar(14),
    ivt_emp_no varchar(50),
    ivt_dtm varchar(14),
    cst_ctc_yn varchar(1),
    alt_tx_clr_yn varchar(1),
    emg_dsp_yn varchar(1),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_tx_alt_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_alt owner to postgres;

create table sf_tx_alt_bs
(
    crdid varchar(19) not null,
    tx_dtm varchar(14) not null,
    tx_ntv_no varchar(12) not null,
    alt_tp_clc varchar(1) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    dao_clc varchar(1),
    alt_rsn_clc varchar(1),
    constraint sf_tx_alt_bs_pk
        primary key (crdid, tx_dtm, tx_ntv_no, alt_tp_clc)
);

alter table sf_tx_alt_bs owner to postgres;

create table sf_tx_au
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50),
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    au_clc varchar(1),
    au_med_clc varchar(1),
    adtl_au_cd varchar(1),
    tlcm_clc varchar(1),
    au_mbphn_no_cryp varchar(255),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    acct_no_cryp varchar(255),
    constraint sf_tx_au_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_au owner to postgres;

create table sf_tx_bamt
(
    tx_base_ym varchar(255) not null,
    tx_guid_no varchar(255) not null,
    acct_id bigint,
    au_inf_gthr_dtm varchar(255),
    bamt varchar(255),
    chg_stm timestamp,
    chg_usr_id varchar(255),
    chnl_clc varchar(255),
    conn_tel_no_cryp varchar(255),
    cst_id bigint,
    fds_chnl_clc varchar(255),
    hts_id varchar(255),
    prcs_rst_clc varchar(255),
    prcs_tp_clc varchar(255),
    reg_stm timestamp,
    reg_usr_id varchar(255),
    svc_cd varchar(255),
    svc_id varchar(255),
    tx_scrn_no varchar(255),
    constraint sf_tx_bamt_pkey
        primary key (tx_base_ym, tx_guid_no)
);

alter table sf_tx_bamt owner to postgres;

create table sf_tx_bnkn
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    opr_mgt_acq_cd varchar(255),
    tx_dt varchar(8),
    bnkn_bizwk_clc varchar(2),
    trrc_clc varchar(255),
    tgrm_no varchar(50),
    login_seq_no varchar(50),
    bnk_prcs_sts_cd varchar(1),
    prcs_amt numeric(15) default 0 not null,
    rltv_bnk_cd varchar(3),
    rltv_acct_id varchar(50),
    rltv_acct_no_cryp varchar(255),
    rltv_bnk_acct_nm varchar(100),
    rcmy_aspr_nm varchar(100),
    rcmy_aspr_rlnm_no_cryp varchar(255),
    rcmy_aspr_tel_no_cryp varchar(255),
    rcmy_psbo_disp_ctnt varchar(255),
    wtdw_psbo_disp_ctnt varchar(255),
    wtdw_afte_blnc_amt numeric(15) default 0 not null,
    wtdw_abl_amt numeric(15) default 0 not null,
    prcs_bnk_cd varchar(3),
    prcs_bnk_brc_cd varchar(20),
    prcs_bnk_brc_nm varchar(100),
    kftc_frd_atnt_cd varchar(2),
    cnl_yn varchar(1),
    au_mans_cd varchar(3),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    rcmy__nm varchar(255),
    rcmy__rlnm_no_cryp varchar(255),
    rcmy__tel_no_cryp varchar(255),
    constraint sf_tx_bnkn_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_bnkn owner to postgres;

create table sf_tx_contact
(
    tx_base_ym varchar(255) not null,
    tx_guid_no varchar(255) not null,
    acct_id bigint,
    au_inf_gthr_dtm varchar(255),
    chg_stm timestamp,
    chg_usr_id varchar(255),
    chnl_clc varchar(255),
    cst_id bigint,
    email_host_nm_cryp varchar(255),
    email_id_cryp varchar(255),
    fds_chnl_clc varchar(255),
    home_tel_area_no_cryp varchar(255),
    home_tel_ecno_cryp varchar(255),
    home_tel_no_cryp varchar(255),
    home_tel_ntn_no_cryp varchar(255),
    hts_id varchar(255),
    mobl_phon_ecno_cryp varchar(255),
    mobl_phon_idnf_no_cryp varchar(255),
    mobl_phon_no_cryp varchar(255),
    mobl_phon_ntn_no_cryp varchar(255),
    offc_tel_area_no_cryp varchar(255),
    offc_tel_ecno_cryp varchar(255),
    offc_tel_no_cryp varchar(255),
    offc_tel_ntn_no_cryp varchar(255),
    prcs_rst_clc varchar(255),
    prcs_tp_clc varchar(255),
    reg_stm timestamp,
    reg_usr_id varchar(255),
    svc_cd varchar(255),
    svc_id varchar(255),
    tx_scrn_no varchar(255),
    constraint sf_tx_contact_pkey
        primary key (tx_base_ym, tx_guid_no)
);

alter table sf_tx_contact owner to postgres;

create table sf_tx_dom_tx_bs
(
    crdid varchar(19) not null,
    tx_dtm varchar(14) not null,
    tx_ntv_no varchar(12) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    tx_clc varchar(1),
    ap_clc varchar(1),
    ap_no varchar(8),
    ap_amt numeric(15),
    mct_clc varchar(1),
    mctno varchar(20),
    mct_crno varchar(10),
    mct_mcc varchar(4),
    constraint sf_tx_dom_tx_bs_pk
        primary key (crdid, tx_dtm, tx_ntv_no)
);

alter table sf_tx_dom_tx_bs owner to postgres;

create table sf_tx_frd
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    alt_tx_rsn_clc varchar(2),
    dsp_bizwk_clc varchar(1),
    dsp_clc varchar(2),
    accd_tp_sclsn_cd varchar(4),
    accd_eps_dtm varchar(14),
    accd_eps_emp_no varchar(50),
    accd_prv_amt1 numeric(15) default 0 not null,
    accd_prv_amt2 numeric(15) default 0 not null,
    accd_prv_amt3 numeric(15) default 0 not null,
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_tx_frd_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_frd owner to postgres;

create table sf_tx_itg
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    conn_tel_no_cryp varchar(255),
    acct_no_cryp varchar(255),
    vctm_acct_scr numeric(4),
    fdbp_scr numeric(4),
    vctm_acct_rsn_scr_cd varchar(6),
    fdbp_rsn_cd varchar(6),
    alt_tx_rsn_clc varchar(2),
    rule_id varchar(8),
    rule_srch_ctnt varchar(255),
    rule_ex_ctnt varchar(255),
    rule_siml_ctnt varchar(255),
    sucs_yn varchar(1),
    reg_clr_clc varchar(1),
    login_mth_clc varchar(2),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_tx_itg_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_itg owner to postgres;

create table sf_tx_jdmn
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    bnkn_bizwk_clc varchar(2),
    trrc_clc varchar(1),
    jdmn_rst_cd varchar(2),
    jdmn_rsn_cd varchar(4),
    rule_id varchar(8),
    ex_rule_id varchar(8),
    plc_src_clc varchar(2),
    plc_tgt_tp_cd varchar(2),
    plc_ctnt varchar(255),
    prcs_amt numeric(15) default 0 not null,
    rltv_bnk_cd varchar(3),
    rltv_acct_no_cryp varchar(255),
    rltv_bnk_acct_nm varchar(100),
    rcmy_aspr_nm varchar(100),
    rcmy_aspr_rlnm_no_cryp varchar(255),
    rcmy_aspr_tel_no_cryp varchar(255),
    rcmy_psbo_disp_ctnt varchar(255),
    wtdw_psbo_disp_ctnt varchar(255),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    plc_tp_cd varchar(255),
    svc_ver varchar(255),
    constraint sf_tx_jdmn_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_jdmn owner to postgres;

create table sf_tx_loan
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    appc_dt varchar(8),
    appc_tm varchar(6),
    ln_amt numeric(15) default 0 not null,
    self_cnfm_mth_cd varchar(2),
    prcs_cnt numeric(9),
    prcs_yn varchar(1),
    ln_tp_dtl_cd varchar(4),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_tx_loan_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_loan owner to postgres;

create table sf_tx_mbl
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    os_type varchar(7),
    uuid varchar(40),
    uuid2 varchar(40),
    os_version varchar(6),
    os_code_name varchar(30),
    manufacturer varchar(20),
    model varchar(30),
    call_state varchar(20),
    data_state varchar(25),
    device_id varchar(20),
    phone_number varchar(15),
    net_country_code varchar(3),
    sim_country_code varchar(3),
    net_op_code varchar(20),
    net_op_code_name varchar(50),
    sim_op_code varchar(20),
    sim_op_code_name varchar(50),
    network_type varchar(20),
    phone_type varchar(20),
    sim_serial_number varchar(30),
    sim_state varchar(25),
    subscriber_id varchar(20),
    conn_network varchar(10),
    mac_address varchar(20),
    is_rooting varchar(1),
    flag_search_app varchar(1),
    is_roaming varchar(1),
    cert_count numeric(3),
    cert_info varchar(255),
    asis_pub_ip varchar(39),
    asis_mc_adr varchar(20),
    asis_hdd_serial varchar(40),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_tx_mbl_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_mbl owner to postgres;

create table sf_tx_ovs_tx_bs
(
    crdid varchar(19) not null,
    tx_dtm varchar(14) not null,
    tx_ntv_no varchar(12) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    tx_clc varchar(1),
    ap_clc varchar(1),
    ap_no varchar(8),
    ap_amt numeric(15),
    mct_cnc varchar(3),
    mct_nm varchar(40),
    constraint sf_tx_ovs_tx_bs_pk
        primary key (crdid, tx_dtm, tx_ntv_no)
);

alter table sf_tx_ovs_tx_bs owner to postgres;

create table sf_tx_pc
(
    tx_guid_no varchar(36) not null,
    tx_base_ym varchar(6) not null,
    au_inf_gthr_dtm varchar(14) not null,
    chnl_clc varchar(3) not null,
    fds_chnl_clc varchar(3) not null,
    svc_id varchar(50) not null,
    svc_cd varchar(50) not null,
    tx_scrn_no varchar(50),
    acct_id varchar(50) not null,
    cst_id varchar(50) not null,
    hts_id varchar(50),
    prcs_tp_clc varchar(2),
    prcs_rst_clc varchar(2),
    log_date varchar(8),
    log_time varchar(9),
    pub_ip varchar(15),
    nat_err_cd varchar(1),
    pub_ip_cntry_cd varchar(12),
    priv_ip1 varchar(15),
    priv_ip2 varchar(15),
    priv_ip3 varchar(15),
    mc_adr1 varchar(17),
    mc_adr2 varchar(17),
    mc_adr3 varchar(17),
    mc_adr4 varchar(17),
    mc_adr5 varchar(17),
    mc_adr6 varchar(17),
    mc_adr7 varchar(17),
    mc_adr8 varchar(17),
    mc_adr9 varchar(17),
    mc_adr10 varchar(17),
    mc_adr1_valid_yn varchar(1),
    mc_adr2_valid_yn varchar(1),
    mc_adr3_valid_yn varchar(1),
    prxy_use_yn varchar(1),
    prxy_ip varchar(50),
    prxy_cntry_cd varchar(2),
    vpn_yn varchar(1),
    vpn_ip varchar(15),
    vpn_cntry_cd varchar(2),
    os_type_cd varchar(2),
    os_ver_cd varchar(12),
    os_sp_cd varchar(2),
    os_cd varchar(2),
    os_guid varchar(40),
    os_vsn_cd varchar(13),
    os_lang_cd varchar(4),
    os_remote_yn varchar(1),
    os_fw_stup_cd varchar(1),
    os_firewall_cd varchar(2),
    bw_vsn_cd varchar(10),
    hdd_model varchar(50),
    hdd_serial varchar(40),
    kbd_type varchar(2),
    usb_serial1 varchar(50),
    usb_serial2 varchar(50),
    usb_serial3 varchar(50),
    mb_serial varchar(50),
    mb_manufacturer varchar(50),
    mb_product_no varchar(50),
    sts_dhack varchar(2),
    sts_key varchar(2),
    elapsed_tm varchar(5),
    log_yn varchar(1),
    cpu_id varchar(30),
    remote_env varchar(40),
    virtual_machine_yn varchar(1),
    asis_pub_ip varchar(39),
    asis_mc_adr varchar(20),
    asis_hdd_serial varchar(40),
    reg_usr_id varchar(20) not null,
    reg_stm date not null,
    chg_usr_id varchar(20) not null,
    chg_stm date not null,
    constraint sf_tx_pc_pk
        primary key (tx_guid_no, tx_base_ym)
);

alter table sf_tx_pc owner to postgres;

create table sf_pf_asis_conn_trm
(
    trm_gthr_clc varchar(6) not null,
    trm_gthr_ntv_id varchar(64) not null,
    cst_id varchar(50) not null,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_pf_asis_conn_trm_key
        primary key (trm_gthr_clc, trm_gthr_ntv_id, cst_id)
);

comment on table sf_pf_asis_conn_trm is '과거수집정보profile';

alter table sf_pf_asis_conn_trm owner to postgres;

create table sf_mt_uue_institution_calendar
(
    tagt_date varchar(8) not null,
    enty_id varchar(1) default '1'::character varying not null,
    hday_yn varchar(1) default 'Y'::character varying,
    constraint sf_mt_uue_institution_calendar_pkey
        primary key (tagt_date, enty_id)
);

alter table sf_mt_uue_institution_calendar owner to postgres;

create table sf_tx_ahnl_msdk_hst
(
    hts_id varchar(50) not null,
    chnl_clc varchar(255) not null,
    tgrm_timestamp varchar(50) not null,
    seq_no integer,
    et_cnt integer,
    tx_dt varchar(8),
    trm_thrt_inf_ctnt varchar(200),
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_tx_ahnl_msdk_hst_pkey
        primary key (hts_id, chnl_clc, tgrm_timestamp)
);

alter table sf_tx_ahnl_msdk_hst owner to postgres;

create table sf_tx_hts_trm_last_conn
(
    hts_id varchar(19) not null,
    trm_inf_tp_cd varchar(6) not null,
    chnl_clc varchar(6) not null,
    au_inf_gthr_dtm varchar(14),
    trm_inf_ctnt text,
    reg_usr_id varchar(20),
    reg_stm date,
    chg_usr_id varchar(20),
    chg_stm date,
    constraint sf_tx_hts_trm_last_conn_pkey
        primary key (hts_id, trm_inf_tp_cd, chnl_clc)
);

alter table sf_tx_hts_trm_last_conn owner to postgres;

create table sf_mt_uwb_channel_cd
(
    chnl_sect_code varchar(3) not null
        constraint sf_mt_uwb_channel_cd_pk
            primary key,
    chnl_sect_name varchar(60),
    chnl_sect_ennm varchar(90),
    chnl_larg_clsn_code varchar(1),
    chnl_larg_clsn_name varchar(30),
    del_yn varchar(1),
    svc_strt_date varchar(8),
    svc_end_date varchar(8),
    etc_rmrk_ctnt varchar(150),
    pcpr_id varchar(20),
    pcpr_ip varchar(39),
    prce_chnl_sect_code varchar(3),
    prce_date_time date
);

alter table sf_mt_uwb_channel_cd owner to postgres;

create table sf_mt_uwb_com_cd
(
    cmn_code_no varchar(40) not null
        constraint sf_mt_uwb_com_cd_pk
            primary key,
    cmn_code_id varchar(5),
    cmn_code_name varchar(150),
    cmn_code_eng_name varchar(300),
    app_down_id varchar(2),
    del_yn varchar(1),
    cmn_code_type_code varchar(10),
    cmn_code_ctnt varchar(6000),
    ini_crtn_yn varchar(1),
    rqst_date_time date,
    pcpr_id varchar(20),
    pcpr_ip varchar(39),
    prce_chnl_sect_code varchar(3),
    prce_date_time date
);

alter table sf_mt_uwb_com_cd owner to postgres;

create table sf_mt_uwb_com_cd_dtl
(
    cmn_code_dtld_no varchar(40) not null
        constraint sf_mt_uwb_com_cd_dtl_pk
            primary key,
    cmn_code_no varchar(40),
    cmn_code_id varchar(5),
    cmn_cdvl varchar(50),
    cmn_cdvl_name varchar(300),
    cmn_code_eng_name varchar(300),
    use_yn varchar(1),
    scrn_disp_ord numeric(10),
    del_yn varchar(1),
    pcpr_id varchar(20),
    pcpr_ip varchar(39),
    prce_chnl_sect_code varchar(3),
    prce_date_time date
);

alter table sf_mt_uwb_com_cd_dtl owner to postgres;


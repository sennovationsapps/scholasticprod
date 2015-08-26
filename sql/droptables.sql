SET @@foreign_key_checks = 0;
drop table if exists donation;

drop table if exists event;

drop table if exists event_pages;

drop table if exists linked_account;

drop table if exists merged_account;

drop table if exists organization;

drop table if exists pfp;

drop table if exists security_role;

drop table if exists team;

drop table if exists token_action;

drop table if exists users;

drop table if exists users_security_role;

drop table if exists users_user_permission;

drop table if exists user_permission;
SET @@foreign_key_checks = 1;
commit;
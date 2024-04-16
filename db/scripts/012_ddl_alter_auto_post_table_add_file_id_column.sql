alter table auto_post
    add column file_id int references file (id)
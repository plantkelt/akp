-- Replace ID=0 by ID=NULL
alter table classe alter column parent drop not null;
update classe set parent=null where parent=0;

-- Remove old taxon-verna2 link (replaced by join table to plante)
delete from taxon where verna2!=0;
alter table taxon drop column verna2;


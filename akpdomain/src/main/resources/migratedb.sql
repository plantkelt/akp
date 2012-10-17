-- Replace ID=0 by ID=NULL
alter table classe alter column parent drop not null;
update classe set parent=null where parent=0;

-- Remove old taxon-verna2 link (old leftover, replaced by join table to plante)
delete from taxon where verna2 != 0;
delete from taxon where plante = 0;
alter table taxon drop column verna2;

-- Add foreign key constraints
alter table bib_verna2 add foreign key (verna2) references verna2 (xid);
alter table bib_verna2 add foreign key (bib) references bib (xid);

alter table plante_verna2 add foreign key (verna2) references verna2 (xid);
alter table plante_verna2 add foreign key (plante) references plante (xid);

alter table plante_xref add foreign key (plante_from) references plante (xid);
alter table plante_xref add foreign key (plante_to) references plante (xid);

alter table taxon add foreign key (plante) references plante (xid);

delete from plante_etiquette where plante not in (select xid from plante);
alter table plante_etiquette add foreign key (plante) references plante (xid);
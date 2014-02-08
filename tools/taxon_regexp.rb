#!/usr/bin/env ruby

require 'pg'

# Parameters
commit = false
search = /\[<i>([a-zA-Z]+)<\/i>\]/
replace = '[\1]'

conn = PG.connect(dbname: 'akp', user: 'akp')
conn.prepare("upd", "UPDATE taxon SET nom=$1 WHERE xid=$2")
conn.prepare("hist", "INSERT INTO hist(xuser,oper,plante,taxon,oldvalue,newvalue) VALUES('r2d2',2,$1,$2,$3,$4)")
conn.exec("SELECT xid, plante, nom FROM taxon") do |result|
	n = 0
	result.each do |row|
		name = row["nom"]
		xid = row["xid"]
		plante = row["plante"]
		if search.match(name)
			name2 = name.gsub(search, replace)
			puts "- #{name}"
			puts "+ #{name2}"
			puts ""
			if commit
				conn.exec_prepared("hist",
					[plante, xid, name, name2])
				conn.exec_prepared("upd", [name2, xid])
			end
			n += 1
			# break if (n >= 2)
		end
	end
	puts "#{n} occurence(s)."
end

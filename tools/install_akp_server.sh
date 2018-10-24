#!/bin/bash

# Installation script to run on debian/ubuntu/systemd servers

echo "=== INSTALLING REQUIRED PACKAGES ==="
apt-get install apache2 postgresql openjdk-11-jre-headless git gradle

echo "=== CREATING/CONFIGURING DATABASE ==="
su postgres -c "createuser akp"
su postgres -c "createdb -E UTF8 -O akp akp"
cp akp/conf/pg_hba.conf /etc/postgresql/*/main/
systemctl reload postgresql

echo "=== DOWNLOAD/COMPILE SOURCE CODE ==="
git clone https://github.com/plantkelt/akp.git
(cd akp && gradle assemble)

echo "=== INSTALL APPLICATION ==="
groupadd akp
useradd akp -g akp
mkdir -p /opt/akp2/conf/
unzip akp/build/distributions/akp-*.zip -d /opt/akp2/
cp akp/conf/log4j.properties /opt/akp2/conf/
cp akp/conf/akp.params /opt/akp2/
ln -fs /opt/akp2/akp-?.?.? /opt/akp2/akp-latest
chown -R akp:akp /opt/akp2

echo "=== INSTALL SYSTEMD SERVICE ==="
cp akp/conf/akp.service /lib/systemd/system
systemctl enable /lib/systemd/system/akp.service

echo "=== CONFIGURE APACHE ==="
cp akp/conf/001-akp.conf /etc/apache2/sites-available/
cp akp/conf/index-default.html /var/www/html/index.html
mkdir -p /var/www/akp/static
cp akp/conf/index.html /var/www/akp/
a2enmod proxy_http && a2ensite 001-akp
systemctl reload apache2

echo "*** PLEASE CONFIGURE MANUALLY:"
echo "*** SMTP PASSWORD in /opt/akp2/akp.params"
echo "*** RUN 'psql akp -U akp < akp.sql' to init the database"
echo "*** RUN 'systemctl start akp' to startup the service."

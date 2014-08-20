//	metrics-influxdb 
//	
//	Written in 2014 by David Bernard <dbernard@novaquark.com> 
//	
//	[other author/contributor lines as appropriate] 
//	
//	To the extent possible under law, the author(s) have dedicated all copyright and
//	related and neighboring rights to this software to the public domain worldwide.
//	This software is distributed without any warranty. 
//	
//	You should have received a copy of the CC0 Public Domain Dedication along with
//	this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>. 
package sandbox;

import java.util.concurrent.TimeUnit;

import metrics_influxdb.Influxdb;
import metrics_influxdb.InfluxdbReporter;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

public class SendToLocalInfluxDB {

  public static void main(String[] args) {
    ScheduledReporter r0 = null;
    ScheduledReporter r1 = null;
    try {
      final MetricRegistry registry = new MetricRegistry();
      r0 = startConsoleReporter(registry);
      r1 = startInfluxdbReporter(registry);

      final Meter mymeter0 = registry.meter("MyMeter.0");
      for (int i = 0; i < 100; i++) {
        mymeter0.mark();
        mymeter0.mark(Math.round(Math.random() * 100.0));
        Thread.sleep(Math.round(Math.random() * 1000.0));
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(1);
    } finally {
      if (r1 != null) {
        r1.report();
        r1.stop();
      }
      if (r0 != null) {
        r0.report();
        r0.stop();
      }
      System.out.println("STOP");
    }
  }

  private static InfluxdbReporter startInfluxdbReporter(MetricRegistry registry) throws Exception {
    final Influxdb influxdb = new Influxdb("127.0.0.1", 8086, "dev", "u0", "u0PWD");
    final InfluxdbReporter reporter = InfluxdbReporter
        .forRegistry(registry)
        .prefixedWith("test")
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .build(influxdb);
    reporter.start(10, TimeUnit.SECONDS);
    return reporter;
  }

  private static ConsoleReporter startConsoleReporter(MetricRegistry registry) throws Exception {
    final ConsoleReporter reporter = ConsoleReporter
        .forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
    reporter.start(1, TimeUnit.MINUTES);
    return reporter;
  }
}

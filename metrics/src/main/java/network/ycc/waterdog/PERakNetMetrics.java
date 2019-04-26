package network.ycc.waterdog;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import network.ycc.raknet.RakNet;
import network.ycc.waterdog.api.metrics.RakNetMetrics;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PERakNetMetrics implements RakNet.MetricsLogger, RakNetMetrics {
    private static double sPerNs = 1.0 / TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

    public static final PERakNetMetrics INSTANCE = new PERakNetMetrics();

    static {
        final int port = 1271; //Utils.getJavaPropertyValue("metrics-port", 1271, Integer::parseInt);
        DefaultExports.initialize();
        try {
            new HTTPServer(port);
        } catch (IOException e) {
            System.err.println("Failed to start HTTP Prometheus metrics exporter on port " + port);
            e.printStackTrace();
        }
    }

    final Counter packetsIn = Counter.build().name("packetsIn").help("packetsIn").register();
    final Counter framesIn = Counter.build().name("framesIn").help("framesIn").register();
    final Counter bytesIn = Counter.build().name("bytesIn").help("bytesIn").register();
    final Counter packetsOut = Counter.build().name("packetsOut").help("packetsOut").register();
    final Counter framesOut = Counter.build().name("framesOut").help("framesOut").register();
    final Counter bytesOut = Counter.build().name("bytesOut").help("bytesOut").register();
    final Counter bytesRecalled = Counter.build().name("bytesRecalled").help("bytesRecalled").register();
    final Counter bytesACKd = Counter.build().name("bytesACKd").help("bytesACKd").register();
    final Counter bytesNACKd = Counter.build().name("bytesNACKd").help("bytesNACKd").register();
    final Counter acksSent = Counter.build().name("acksSent").help("acksSent").register();
    final Counter nacksSent = Counter.build().name("nacksSent").help("nacksSent").register();
    final Histogram rtt = Histogram.build().name("rtt").help("RTT in seconds").register();
    final Histogram burstTokens = Histogram.build().name("burstTokens").help("Burst tokens").register();
    final Gauge rttGauge = Gauge.build().name("rttGauge").help("RTT in seconds").register();
    final Counter outPrePacket = Counter.build().name("out_pre_comp_packet").help("Game packets sent (pre compression)").register();
    public final Counter outPreComp = Counter.build().name("out_pre_comp").help("Bytes out pre-compression").register();
    public final Counter outPostComp = Counter.build().name("out_post_comp").help("Bytes out post-compression").register();

    private PERakNetMetrics() {}

    public void packetsIn(int i) {
        packetsIn.inc(i);
    }

    public void framesIn(int i) {
        framesIn.inc(i);
    }

    public void bytesIn(int i) {
        bytesIn.inc(i);
    }

    public void packetsOut(int i) {
        packetsOut.inc(i);
    }

    public void framesOut(int i) {
        framesOut.inc(i);
    }

    public void bytesOut(int i) {
        bytesOut.inc(i);
    }

    public void bytesRecalled(int i) {
        bytesRecalled.inc(i);
    }

    public void bytesACKd(int i) {
        bytesACKd.inc(i);
    }

    public void bytesNACKd(int i) {
        bytesNACKd.inc(i);
    }

    public void acksSent(int i) {
        acksSent.inc(i);
    }

    public void nacksSent(int i) {
        nacksSent.inc(i);
    }

    public void measureRTTns(long l) {
        rtt.observe(l * sPerNs);
    }

    public void measureBurstTokens(int n) {
        burstTokens.observe(n * 0.001);
    }

    public void measureRTTnsStdDev(long n) {
        rttGauge.set(n * sPerNs);
    }

    @Override
    public void preCompressionBytes(int i) {
        outPreComp.inc(i);
    }

    @Override
    public void postCompressionBytes(int i) {
        outPostComp.inc(i);
    }

    @Override
    public void preCompressionPacket(int i) {
        outPrePacket.inc(i);
    }
}

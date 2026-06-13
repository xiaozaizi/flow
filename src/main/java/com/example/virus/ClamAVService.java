package com.example.virus;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ClamAVService {

    // Simple wrapper that calls clamscan command-line tool. In production consider using clamd or a hosted scanning service.
    public boolean scan(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder("clamscan", "--no-summary", file.getAbsolutePath());
            Process p = pb.start();
            int rc = p.waitFor();
            return rc == 0; // 0 = no virus found; 1 = virus found; >1 = error
        } catch (IOException | InterruptedException e) {
            // If scanning not available, log and allow by default in PoC
            System.err.println("ClamAV scan failed or not available: " + e.getMessage());
            return true;
        }
    }
}

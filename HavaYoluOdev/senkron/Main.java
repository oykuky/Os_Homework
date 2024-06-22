import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

class SeatClass {
    boolean isReserved;
    int seatNum;

    SeatClass(int seatNum) {
        this.seatNum = seatNum;
        this.isReserved = false;
    }
}

class FlightClass {
    List<SeatClass> seats = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);  
    private final JTextArea output;
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    FlightClass(int numberOfSeats, JTextArea output) {
        this.output = output;
        for (int i = 1; i <= numberOfSeats; i++) {
            seats.add(new SeatClass(i));
        }
    }

    private void appendOutput(String message) {
        SwingUtilities.invokeLater(() -> {
            String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            output.append("Zaman: " + timeStamp + " " + message + "\n\n");
        });
    }

    public boolean makeReservation(int seatNum) {
        writeLock.lock();
        try {
            String requestMessage = "Koltuk numarası " + seatNum + " rezerve isteği";
            SeatClass seat = seats.get(seatNum - 1);
            String resultMessage;
            // koltuk rezerve edilir
            if (!seat.isReserved) {  
                seat.isReserved = true;
                resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " başarıyla rezerve edildi.";
            } else {
                resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " önceden rezerve edildildiğinden rezerve edilemez.";
            }
            appendOutput(requestMessage + "\n" + resultMessage);
            return seat.isReserved;
        } finally {
            writeLock.unlock();
        }
    }

    public void queryReservation(int seatNum) {
        readLock.lock();
        try {
            String requestMessage = "Koltuk numarası " + seatNum + " sorgulama isteği";
            SeatClass seat = seats.get(seatNum - 1);
            String resultMessage = Thread.currentThread().getName() + " Boş koltuklar bakılıyor. Uygun koltuklar :\n" + getSeatStates();
            appendOutput(requestMessage + "\n" + resultMessage);
        } finally {
            readLock.unlock();
        }
    }

    public boolean cancelReservation(int seatNum) {
        writeLock.lock();
        try {
            String requestMessage = "Koltuk numarası " + seatNum + " iptal isteği";
            SeatClass seat = seats.get(seatNum - 1);
            String resultMessage;
            if (seat.isReserved) {
                seat.isReserved = false;
                resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " rezervesi başarıyla iptal edildi.";
            } else {
                resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " için rezervasyon iptal edilemedi çünkü bu koltuk rezerve edilmemişti.";
            }
            appendOutput(requestMessage + "\n" + resultMessage);
            return !seat.isReserved;
        } finally {
            writeLock.unlock();
        }
    }

    private String getSeatStates() {
        StringBuilder seatStates = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            seatStates.append("Koltuk No ").append(i + 1).append(" : ").append(seats.get(i).isReserved ? "1" : "0").append(" ");
        }
        return seatStates.toString();
    }
}

class ReaderThread extends Thread {
    FlightClass flight;

    ReaderThread(FlightClass flight) {
        this.flight = flight;
    }

    public void run() {
        flight.queryReservation(2);
    }
}

class WriterThread extends Thread {
    FlightClass flight;

    WriterThread(FlightClass flight) {
        this.flight = flight;
    }

    public void run() {
        flight.makeReservation(2);
        flight.makeReservation(2);
        flight.makeReservation(2);
    }
}

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Havayolu Rezervasyon Sistemi");
        JTextArea output = new JTextArea(50, 60);
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        FlightClass flight = new FlightClass(10, output);
        WriterThread writer1 = new WriterThread(flight);
        WriterThread writer2 = new WriterThread(flight);
        WriterThread writer3 = new WriterThread(flight);
        ReaderThread reader1 = new ReaderThread(flight);
        ReaderThread reader2 = new ReaderThread(flight);
       

        reader2.start();
        writer1.start();
        writer2.start();
        writer3.start();
        reader1.start();
}
}


  //Problem çözümü:
    //ReentrantReadWriteLock ile eğer bir yazıcı bekliyorsa yeni okuyucuların girmesine izin verilmez, yazıcılara öncelik verilir
    //Bu şekilde bir yazıcı bekliyorsa, okuyucuların girmesine izin verilmez ve yazıcıların sürekli ertelenmesi engellenir. 
    //Bu yazıcıların açlığını önler okuyucuların aşırı öncelik almasının da önüne geçer.
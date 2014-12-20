package org.greenvilleoaks

import org.supercsv.io.CsvMapReader
import org.supercsv.io.CsvMapWriter
import org.supercsv.io.ICsvMapReader
import org.supercsv.io.ICsvMapWriter
import org.supercsv.prefs.CsvPreference

final class Csv {
    private final String[] header
    private final String fileName

    /**
     * Create an instance of the class whose headers are derived from the content of the file
     * @param fileName The name of the CSV file
     */
    public Csv(final String fileName) {
        this.fileName = fileName
        this.header = extractHeader()
    }


    /**
     * Create an instance of the class whose headers are derived from the content of the file if it exists or
     * from the headers passed into the constructor if the file doesn't exist.
     * @param fileName The name of the CSV file
     * @param header The headers to use if the file doesn't exist
     */
    public Csv(final String fileName, final String[] header) {
        this.fileName = fileName
        if (new File(fileName).exists()) {
            this.header = extractHeader()
        }
        else {
            this.header = header
        }
    }

    /**
     * @return the headers from the CSV file if it exists or null if it doesn't
     */
    private String[] extractHeader() {
        String[] hdr = null
        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.STANDARD_PREFERENCE);

            hdr = mapReader.getHeader(true);
        }
        finally {
            if (mapReader != null) mapReader.close();
        }
        return hdr
    }



    /**
     * @return A list where each row is a map containing a key that is the header and an object which is its value
     */
    public List<Map<String, String>> load() {
        if (!(new File(fileName).exists())) return []

        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.STANDARD_PREFERENCE);

            List<Map<String, String>> members = []
            Map<String, String> member;
            while( (member = mapReader.read(header)) != null ) {
                boolean blankRow = true;
                member.values().each { if (it != null) blankRow = false}
                if (!blankRow) members << member
            }

            // Remove header row
            return members.minus(members[0])
        }
        finally {
            if( mapReader != null ) mapReader.close();
        }
    }



    /**
     * Store a list of maps to a file using the headers as keys to each row's map
     * @param listOfMaps
     */
    public void store(final List<Map<String, String>> listOfMaps) {
        ICsvMapWriter mapWriter = null;
        try {
            mapWriter = new CsvMapWriter(new FileWriter(fileName), CsvPreference.STANDARD_PREFERENCE)
            mapWriter.writeHeader(header)
            listOfMaps.each {mapWriter.write(it, header)}
        }
        finally {
            if( mapWriter != null ) mapWriter.close();
        }
    }
}

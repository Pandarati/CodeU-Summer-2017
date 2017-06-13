package codeu.chat.util;

import java.io.IOException;

public final class Tokenizer {
   
   private final String source;
   private StringBuilder token;
   private int at;

   public Tokenizer(String source) {
      token = new StringBuilder();
      this.source = source;
      at = 0;
   }

   public String next() throws IOException {
      //Skips all of the leading whitespaces that proceeeds the characters.
      while (remaining() > 0 && Character.isWhitespace(peek())) {
      read();  // ignore the result and reads because we already know that it is a whitespace character
      }
      if (remaining() <= 0) {
         return null;
      } else if (peek() == '"') {
         return readWithQuotes();
      } else {
         return readWithNoQuotes();
      }

   }

   //input is tokenized by word when a whitespace is read
   private String readWithNoQuotes() throws IOException {
      token.setLength(0);  // clear the token
      while (remaining() > 0 && !Character.isWhitespace(peek())) {
         token.append(read());
      }
      return token.toString();
   }

   //input is tokenized by words inside quotations
   private String readWithQuotes() throws IOException {
      token.setLength(0);  // clear the token
      if (read() != '"') {
         throw new IOException("Strings must start with opening quote");
      }
      while (peek() != '"') {
         token.append(read());
      }
      read(); // read the closing the quote that allowed us to exit the loop
      return token.toString();
   }


   // determines how much more input we have left to parse
   private int remaining() {
      return source.length() - at;
   }

   // returns what character is currently being parsed
   private char peek() throws IOException {
      if (at < source.length()) {
         return source.charAt(at);
      } else {
      // throw an exception
         throw new IOException();
      }
   }

   // reads the current character
   private char read() throws IOException {
      final char c = peek();
      at += 1;
      return c;
   }

}
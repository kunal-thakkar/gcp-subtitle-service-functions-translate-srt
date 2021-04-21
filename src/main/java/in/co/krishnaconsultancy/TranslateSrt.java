package in.co.krishnaconsultancy;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.testing.RemoteTranslateHelper;

import java.io.*;

public class TranslateSrt implements HttpFunction {
  @Override
  public void service(HttpRequest request, HttpResponse response) throws Exception {
    HttpRequest.HttpPart part = request.getParts().get("file");
    if(part != null) {
      try(BufferedReader rdr = part.getReader();
          BufferedWriter writer = response.getWriter()) {
        String targetLanguage = request.getFirstQueryParameter("targetLanguage").orElse("hi");
        RemoteTranslateHelper helper = RemoteTranslateHelper.create();
        Translate translate = helper.getOptions().getService();
        Translate.TranslateOption target = Translate.TranslateOption.targetLanguage(targetLanguage);
        writer.write(rdr.lines().parallel().map(line -> (
            line.trim().length() == 0 ||
            line.matches("^\\d{1,9}$") ||
            line.matches("^\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}$")
            ) ? line : translate.translate(line.trim(), target).getTranslatedText()
        ).reduce((o, o2) -> o.concat("\n").concat(o2)).get());
      }
    }
  }
}

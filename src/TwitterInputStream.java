import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.FilterQuery;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterInputStream{
	
	public static void main(String[] args) throws IOException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("jwjh75DB7V3PUiJ6lSFmr69Uk");
        cb.setOAuthConsumerSecret("v0FfZYcw2t6NZjRvtkJ8r0Lgrke8BsInuJviOttni4FzjAnobk");
        cb.setOAuthAccessToken("2383392254-zDEW6587TLHzF2fnmz3xU3oGMucWbyvm88llgNh");
        cb.setOAuthAccessTokenSecret("iugDq2K8pPoSmBYHzlYOQJik2XKqrdtf2hCdjU1y5xh08");

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStatus(Status status) {
            	//TODO: status.getLang();
            	for (MediaEntity mediaEntity : status.getMediaEntities())
                {
            		String type = mediaEntity.getType();
                	//System.out.println("Type: "+type);
                	if(type.trim().equalsIgnoreCase("photo")){
                		User user = status.getUser();
                        
                        // gets Username
                        String username = status.getUser().getScreenName();
                        System.out.println("Username: " + username);
                        String profileLocation = user.getLocation();
                        System.out.println("Profile Location: " + profileLocation);
                        long tweetId = status.getId(); 
                        System.out.println("Tweet ID: "+tweetId);
                        String content = status.getText();
                        System.out.println("Content: "+content);
                        String mediaURL = mediaEntity.getMediaURL();
                        System.out.println("URL: "+mediaURL +"\n");   
                        try {
                        	URL url = new URL(mediaURL);
							InputStream in = new BufferedInputStream(url.openStream());
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							byte[] buf = new byte[1024];
							int n = 0;
							
							while (-1!=(n=in.read(buf)))
							{
							   out.write(buf, 0, n);
							}
							out.close();
							in.close();
							byte[] response = out.toByteArray();
							
							FileOutputStream fos = new FileOutputStream(tweetId+".jpg");
				       	
							fos.write(response);
							fos.close();
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                	}
                }
                
            }

            @Override
            public void onTrackLimitationNotice(int arg0) {
                // TODO Auto-generated method stub

            }

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}

        };
        
        FilterQuery fq = new FilterQuery();
        
        String keywords[] = {"monument","monuments","university"};
        String[] lang = {"en"};
        fq.track(keywords);
        fq.language(lang);
        
        twitterStream.addListener(listener);
        twitterStream.sample();
        //twitterStream.filter(fq);
	}
}

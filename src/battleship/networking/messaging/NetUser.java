package battleship.networking.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

// This class is meant to serve as a unique object to identify individual users (could be multiple users on same device)
// TODO: Serialize and save between sessions.

public class NetUser implements Externalizable {

	// Should only be used when deserializing
	public NetUser() { }
	
	public static String[] COMMON_FIRST_NAMES = new String[] { // Taken from https://www.ssa.gov/oact/babynames/decades/century.html
			"James", "Robert", "John", "Michael", "David", "William", "Richard", "Joseph", "Thomas", "Charles", "Christopher",
			"Daniel", "Matthew", "Anthony", "Mark", "Donald", "Steven", "Paul", "Andrew", "Joshua", "Kenny", "Kevin", "Brian",
			"George", "Timothy", "Ronald", "Edward", "Jason", "Jeffrey", "Ryan", "Jacob", "Gary", "Nicholas", "Eric", "Johnathon",
			"Stephen", "Larry", "Justin", "Scott", "Brandon", "Benjamin", "Samuel", "Gregory", "Alexander", "Frank", "Patrick",
			"Raymond", "Jack", "Dennis", "Jerry", "Tyler", "Aaron", "Jose", "Adam", "Nathan", "Henry", "Douglas", "Zachary",
			"Peter", "Kyle", "Ethan", "Walter", "Noah", "Jeremy", "Christian", "Keith", "Roger", "Terry", "Gerald", "Harold", "Sean",
			"Austin", "Carl", "Arthur", "Lawrence", "Dylan", "Jesse", "Jordan", "Bryan", "Billy", "Joe", "Bruce", "Gabriel", "Logan",
			"Albert", "Willie", "Alan", "Juan", "Wayne", "Elijah", "Randy", "Roy", "Vincent", "Ralph", "Eugene", "Russell", "Bobby",
			"Mason", "Philip", "Louis"
	};
	
	
	// Two fields that characterize instances of this class
	public String name = null;
	public int id = -1;
	private NetUser(String n, int i) {
		name = n;
		id = i;
	}
	public static class Factory {
		private static Random r = new Random();
		public static NetUser random() {
			return randomID(COMMON_FIRST_NAMES[(int) (Math.random()*COMMON_FIRST_NAMES.length)]);
		}
		public static NetUser randomID(String name) {
			return new NetUser(name, r.nextInt(9000)+1000);
		}
		public static NetUser make(String name, int id) {
			return new NetUser(name, id);
		}
	}
	
	@Override
	public String toString() { return name + "#" + id; }
	
	@Override
	public int hashCode() { return id ^ name.hashCode(); }
	@Override
	public boolean equals(Object o) {
		if (o instanceof NetUser) {
			NetUser nuo = (NetUser) o;
			return nuo.id == this.id && nuo.name.equals(this.name);
		};
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(name.length());
		out.writeChars(name);
		out.writeInt(id);
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int nameLen = in.readInt();
		name = "";
		for (int i=0;i<nameLen;i++) name += in.readChar();
		id = in.readInt();
	}
	
}

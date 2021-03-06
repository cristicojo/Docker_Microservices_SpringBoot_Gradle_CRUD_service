package employees.modelMongo;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;


@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "employees")
public class Employees {

	@Id
	public String _id;

	private String first_name;
	private String last_name;
	private Date dob;
	private String direct_manager;
	private double salary;
	private String department;
}

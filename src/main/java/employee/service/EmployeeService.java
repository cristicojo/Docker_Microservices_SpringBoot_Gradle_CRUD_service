package employee.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import employee.entity.Employee;
import employee.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

	private final EmployeeRepository repo;
	private final MongoTemplate mongoTemplate;



	public List<Employee> getAll() {
		return repo.findAll();
	}

	public Employee getById(String id) {
		return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Could not found an employee with id: " + id));
	}


	public Employee create(Employee newEmp) {
		return repo.save(newEmp);
	}


	public List<Employee> createAll(List<Employee> employeeList) {
		return repo.saveAll(employeeList);
	}


	public Employee updateById(String id, Employee newEmp) {

		Optional<Employee> optional = repo.findById(id);

		if (!(optional.isPresent())) {
			throw new ResourceNotFoundException("Could not found an employee with id: " + id);
		}

		Employee oldEmp = optional.get();
		oldEmp.setFirstName(newEmp.getFirstName());
		oldEmp.setLastName(newEmp.getLastName());
		oldEmp.setDob(newEmp.getDob());
		oldEmp.setDirectManager(newEmp.getDirectManager());
		oldEmp.setSalary(newEmp.getSalary());
		oldEmp.setDepartment(newEmp.getDepartment());

		return repo.save(oldEmp);
	}


	public void remove(String id) {
		repo.delete(repo.findById(id).orElseThrow(() ->
				new ResourceNotFoundException("Could not found an employee with id: " + id)));
	}


	public void removeAll() {
		repo.deleteAll();
	}


	//the employee who has the biggest salary in the given department
	public Employee maxSalary(String department) {

		List<Employee> list = repo.findByDepartment(department);
		Employee employee = Employee.builder().build();

		if (list.isEmpty()) {
			throw new ResourceNotFoundException("Could not found department with the name: " + department);
		}

		double max = list.get(0).getSalary();
		for (Employee e : list) {
			if (max <= e.getSalary()) {
				max = e.getSalary();
				employee = e;
			}
		}

		return employee;
	}

	//the manager who has the most "direct" employees coordinated by him
	public Employee getDirectManager() {

		List<Employee> employeeList = repo.findAll();
		List<String> direct_ManagerList = new ArrayList<>();

		for (Employee e : employeeList) {
			direct_ManagerList.add(e.getDirectManager());
		}

		Employee manager = Employee.builder().build();

		//find the most repeated manager in direct_manager column
		Map<String, Integer> map = new HashMap<>();

		for (String st : direct_ManagerList) {
			String key = st.toLowerCase();
			if (map.get(key) != null) {
				Integer value = map.get(key) + 1;
				map.put(key, value);
			} else {
				map.put(key, 1);
			}
		}

		//find the key which has the maximum value from the map
		String maxEntry = Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();

		for (Employee e : employeeList) {
			if (maxEntry.equalsIgnoreCase(e.getDirectManager())) {
				manager = e;
			}
		}

		return manager;
	}


	//BONUS POINTS
	//load a json file
	public void importJsonFile() {

		File fileJson = new File("/Users/cristicojocaru/Documents/old/Untitled_93.json");

		//Read each line of the json file. Each file is one observation document.
		List<Document> observationDocuments = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fileJson.getPath()))) {
			String line;
			while ((line = br.readLine()) != null) {
				observationDocuments.add(Document.parse(line));
			}
		} catch (IOException ex) {
			ex.getStackTrace();
		}

		mongoTemplate.getCollection("employees").insertMany(observationDocuments);
	}


	//paging
	public Page<Employee> getPage(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);
		return repo.findAll(pageable);
	}


	//top n best paid employees in a given department
	public List<Employee> topBestPaid(String department, int n) {

		Criteria find = Criteria.where("department").is(department);
		Query query = new Query().addCriteria(find).with(Sort.by(Sort.Direction.DESC, "salary")).limit(n);
		List<Employee> employeeList = mongoTemplate.find(query, Employee.class);

		if (employeeList.isEmpty()) {
			throw new ResourceNotFoundException("Could not found department with the name: " + department);
		}

		return employeeList;
	}


	//management tree: from the top CEO to the lowest employee
	public List<Employee> managementTree() {

		Query query = new Query().with(Sort.by(Sort.Direction.DESC, "salary"));
		return mongoTemplate.find(query, Employee.class);
	}
}
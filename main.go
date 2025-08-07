package main
import (
	"fmt"
	"os"
	"sync"
	"time"
)
const numWorkers = 4
const numTasks = 20
func main() {
	fmt.Println("System started.")
	tasks := make(chan string, numTasks)
	for i := 1; i <= numTasks; i++ {
		tasks <- fmt.Sprintf("Task-%d", i)
	}
	close(tasks) 
	var wg sync.WaitGroup
	var mutex sync.Mutex
	results := make([]string, 0)
	for i := 1; i <= numWorkers; i++ {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()
			workerName := fmt.Sprintf("Worker-%d", id)

			for task := range tasks {
				fmt.Printf("%s started processing %s\n", workerName, task)
				processTask()
				result := fmt.Sprintf("%s completed %s", workerName, task)

				mutex.Lock()
				results = append(results, result)
				mutex.Unlock()

				fmt.Println(result)
			}
			fmt.Printf("%s finished all tasks.\n", workerName)
		}(i)
	}
	wg.Wait()
	err := writeResultsToFile("output_go.txt", results)
	if err != nil {
		fmt.Println("Error writing output file:", err)
	} else {
		fmt.Println("Results saved to output_go.txt")
	}

	fmt.Println("System completed.")
}
func processTask() {
	time.Sleep(500 * time.Millisecond)
}
func writeResultsToFile(filename string, data []string) error {
	file, err := os.Create(filename)
	if err != nil {
		return err
	}
	defer file.Close()

	for _, line := range data {
		_, err := file.WriteString(line + "\n")
		if err != nil {
			return err
		}
	}
	return nil
}
